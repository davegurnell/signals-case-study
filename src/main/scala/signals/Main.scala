package signals

import cats.effect.IO
import fs2._
import fs2.io.udp._
import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext.Implicits.global

// Connect from the command line using:
//     nc -u localhost 8888
object Main {
  implicit val group = AsynchronousSocketGroup()

  val address = new InetSocketAddress(8888)

  // TODO: This doesn't listen for disconnects:
  open[IO](address).flatMap { serverSocket =>
    Handlers.handleStream(serverSocket.reads())
      .evalMap(serverSocket.write(_))
      .drain
  }.runLog.unsafeRunSync
}

object Handlers {
  def handleStream(inputStream: Stream[IO, Packet]): Stream[IO, Packet] =
    inputStream.flatMap(handlePacket)

  def handlePacket(inputPacket: Packet): Stream[IO, Packet] = {
    val inputString: String = packetToString(inputPacket)
    val outputStrings: Stream[IO, String] = handleString(inputString)
    val outputPackets: Stream[IO, Packet] = outputStrings.map(stringToPacket(inputPacket.remote))
    outputPackets
  }

  def handleString(input: String): Stream[IO, String] =
    Stream.eval(IO(input.reverse))

  private def packetToString(packet: Packet): String =
    new String(packet.bytes.toBytes.values, "utf-8").trim

  private def stringToPacket(remote: InetSocketAddress)(string: String): Packet =
    Packet(remote, Chunk.Bytes(s"$string\n".getBytes("utf-8")))
}
