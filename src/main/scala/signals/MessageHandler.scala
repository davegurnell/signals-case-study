package signals

import cats.effect.IO
import fs2.Stream

trait MessageHandler {
  def apply(input: String): Stream[IO, String]
}

object MessageHandler {
  def pure(func: String => Stream[IO, String]): MessageHandler =
    new MessageHandler {
      def apply(input: String): Stream[IO, String] =
        func(input)
    }

  def simple(func: String => String): MessageHandler =
    pure(input => Stream.eval(IO(func(input))))

  val reverseMessages: MessageHandler =
    simple(_.reverse)
}