name := "sensor-stats-calculator"

version := "0.1"

scalaVersion := "2.13.14"

coverageEnabled := true

libraryDependencies ++= Seq(
  "io.monix" %% "monix" % "3.4.0",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test
)
