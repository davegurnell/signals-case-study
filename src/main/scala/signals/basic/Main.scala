package signals.basic

// Connect from the command line using:
//
//     sbt runMain signals.basic.Main
//
object Main {
  def main(args: Array[String]): Unit = {
    val inputSignal = Stream.continually(io.StdIn.readLine)
    val outputSignal = SignalProcessor(inputSignal)
    outputSignal.foreach(println)
  }
}
