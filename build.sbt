name := "akka-streams-basic"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
)

mainClass in Compile := Some("app.Main")