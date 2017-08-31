package signals

import cats.effect.IO
import fs2._
import fs2.io.udp._
import java.net.InetSocketAddress
import scala.concurrent.ExecutionContext.Implicits.global

// Connect from the command line using:
//     nc -u localhost 8888
object Main extends App {
  implicit val group = AsynchronousSocketGroup()

  val address = new InetSocketAddress(8888)

  val messageHandler = MessageHandler.reverseMessages

  // TODO: This doesn't listen for disconnects:
  open[IO](address).flatMap { serverSocket =>
    handleStream(serverSocket.reads())
      .evalMap(serverSocket.write(_))
      .drain
  }.runLog.unsafeRunSync

  def handleStream(inputStream: Stream[IO, Packet]): Stream[IO, Packet] =
    inputStream.flatMap(handlePacket)

  def handlePacket(inputPacket: Packet): Stream[IO, Packet] = {
    val inputString: String = packetToString(inputPacket)
    val outputStrings: Stream[IO, String] = messageHandler(inputString)
    val outputPackets: Stream[IO, Packet] = outputStrings.map(stringToPacket(inputPacket.remote))
    outputPackets
  }

  private def packetToString(packet: Packet): String =
    new String(packet.bytes.toBytes.values, "utf-8").trim

  private def stringToPacket(remote: InetSocketAddress)(string: String): Packet =
    Packet(remote, Chunk.Bytes(s"$string\n".getBytes("utf-8")))
}
