import com.typesafe.sbt.SbtAspectj._

name := "curri-akka"
organization := "curri"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaV = "2.4.3"
  // val akkaV = "10.0.0"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,

    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
    "com.typesafe.play" % "play-json_2.11" % "2.4.0-M2",
    "ch.qos.logback" % "logback-classic" % "1.1.2",

    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test"
  )
}
Revolver.settings

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"


mainClass in(Compile, run) := Some("Boot")