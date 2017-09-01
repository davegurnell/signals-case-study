package signals.fs2

import java.net.InetSocketAddress

import cats.effect.IO
import fs2._
import fs2.io.udp._
import signals.fs2.SignalProcessor.Signal

import scala.concurrent.ExecutionContext.Implicits.global

// Connect from the command line using:
//
//     nc -u localhost 8888
//
object Main {
  implicit val group = AsynchronousSocketGroup()

  val address = new InetSocketAddress(8888)

  def main(args: Array[String]): Unit =
    connect(address).runLog.unsafeRunSync

  def connect(address: InetSocketAddress): Stream[IO, Stream[IO, Nothing]] =
    // TODO: This doesn't listen for disconnects:
    open[IO](address).flatMap { serverSocket =>
      inputToOutput(serverSocket.reads())
        .evalMap(serverSocket.write(_))
        .drain
    }

  def inputToOutput(stream: Stream[IO, Packet]): Stream[IO, Packet] =
    stream.flatMap { (inputPacket: Packet) =>
      val inputStream: Stream[IO, Packet] =
        Stream
          .eval(IO(inputPacket))
          .map(debug("inputPacket"))

      val inputSignal: Stream[IO, String] =
        inputStream
          .map(packetToString)
          .map(debug("inputString"))

      val outputSignal: Stream[IO, String] =
        SignalProcessor(inputSignal)
          .map(debug("outputString"))

      val outputStream: Stream[IO, Packet] =
        outputSignal
          .map(stringToPacket(_, inputPacket.remote))
          .map(debug("outputPacket"))

      outputStream
    }

  def packetToString(packet: Packet): String = {
    new String(packet.bytes.toBytes.values, "utf-8").trim
  }

  def stringToPacket(string: String, remote: InetSocketAddress): Packet =
    Packet(remote, Chunk.Bytes(s"$string\n".getBytes("utf-8")))

  def debug[A](msg: String)(value: A): A = {
    println(msg + " " + value)
    value
  }
}
