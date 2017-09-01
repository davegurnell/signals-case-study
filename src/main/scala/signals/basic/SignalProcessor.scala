package signals.basic

object SignalProcessor {
  type Signal[A] = Stream[A]

  def apply(input: Signal[String]): Signal[String] =
    input.map(_.reverse)
}
