package signals.fs2

import cats.effect.IO
import fs2.Stream

object SignalProcessor {
  type Signal[A] = Stream[IO, A]

  def apply(input: Signal[String]): Signal[String] =
    input.map(_.reverse)
}
