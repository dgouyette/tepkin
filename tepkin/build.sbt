name := "tepkin"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "com.typesafe.akka" %% "akka-actor" % Dependencies.akkaV,
  "com.typesafe.akka" %% "akka-stream-experimental" % Dependencies.akkaStreamV,
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % Test,
  "org.scalatest" %% "scalatest" % Dependencies.scalatestV % Test
)
