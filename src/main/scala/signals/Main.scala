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
    Stream.eval(serverSocket.localAddress)
      .map(_.getPort)
      .flatMap { serverPort =>
        println(serverPort)
        val serverAddress = new InetSocketAddress(serverPort)
        val server = serverSocket
          .reads()
          .evalMap { inputPacket =>
            serverSocket.write(handle(inputPacket))
          }
          .drain
//          val client = open[IO]()
//            .flatMap { clientSocket =>
//              Stream(Packet(serverAddress, msg))
//                .covary[IO]
//                .to(clientSocket.writes())
//                .drain ++
//              Stream.eval(clientSocket.read())
//            }
      server //.mergeHaltBoth(client)
    }
  }.runLog.unsafeRunSync

  def handle(inputPacket: Packet): Packet = {
    val inputString = new String(inputPacket.bytes.toBytes.values, "utf-8")
    val outputString = inputString.trim.reverse + "\n"
    val outputPacket = Packet(inputPacket.remote, Chunk.Bytes(outputString.getBytes("utf-8")))
    outputPacket
  }
}
