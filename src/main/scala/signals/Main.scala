package signals

import cats.effect.IO
import fs2._
import fs2.io.udp._
import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
  implicit val group = AsynchronousSocketGroup()
  val address = new InetSocketAddress(8888)

  open[IO](address).flatMap { serverSocket =>
    handleStream(serverSocket.reads())
      .evalMap(serverSocket.write(_))
      .drain
  }.runLog.unsafeRunSync

  def handleStream(inputStream: Stream[IO, Packet]): Stream[IO, Packet] =
    inputStream.flatMap(handlePacket)

  def handlePacket(inputPacket: Packet): Stream[IO, Packet] =
    handleString(packetToString(inputPacket))
      .map(stringToPacket(inputPacket.remote))

  def handleString(input: String): Stream[IO, String] =
    Stream.eval(IO(input.reverse))

  private def packetToString(packet: Packet): String =
    new String(packet.bytes.toBytes.values, "utf-8").trim

  private def stringToPacket(remote: InetSocketAddress)(string: String): Packet =
    Packet(remote, Chunk.Bytes(s"$string\n".getBytes("utf-8")))
}
