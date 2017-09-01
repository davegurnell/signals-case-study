name         := "signals-case-study"
version      := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.3"

// For the FS2 demo:
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest"   % "3.0.3" % Test
)

// For the Akka Streams demo:
libraryDependencies ++= Seq(
  "co.fs2"        %% "fs2-core"    % "0.10.0-M6",
  "co.fs2"        %% "fs2-io"      % "0.10.0-M6",
  "org.scodec"    %% "scodec-bits" % "1.1.5"
)
