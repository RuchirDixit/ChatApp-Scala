
name := "Chat-App-Akka"

version := "0.1"

scalaVersion := "2.12.2"

//coverageExcludedPackages := "*Test.scala"

coverageEnabled := true

libraryDependencies ++= Seq("com.typesafe.akka" %% "akka-actor" % "2.5.20",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.20" % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.20",
  "com.typesafe.akka" %% "akka-http" % "10.1.7",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.1.7" % Test,
  "com.typesafe.play" %% "play-json" % "2.6.7",
  "de.heikoseeberger" %% "akka-http-play-json" % "1.20.0",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "org.scalatest" %% "scalatest" % "3.2.2" % Test,
  "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5",
  "com.github.daddykotex" %% "courier" % "3.0.0-M2",
  "com.nimbusds" % "nimbus-jose-jwt" % "9.3",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)