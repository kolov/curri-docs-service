import sbt.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

lazy val commonSettings = Seq(
  name := "curri-docs",
  organization := "curri",
  version := "1.0",
  scalaVersion := "2.12.4"
)




Revolver.settings

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"

mainClass in(Compile, run) := Some("DocsServiceApp")
enablePlugins(JavaAppPackaging)


// from http://stackoverflow.com/questions/25144484/sbt-assembly-deduplication-found-error
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}


lazy val mainProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= {
    val akkaVersion = "2.4.17"
    val circeVersion  = "0.8.0"

    Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka"          %% "akka-actor"               % akkaVersion,
      "com.typesafe.akka"          %% "akka-slf4j"               % akkaVersion,
      "com.typesafe.akka"          %% "akka-http"                % "10.0.6",
      "de.heikoseeberger"          %% "akka-http-circe"          % "1.15.0",
      "io.circe"                   %% "circe-generic"            % circeVersion,
      "io.circe"                   %% "circe-generic-extras"     % circeVersion,
      "io.circe"                   %% "circe-java8"              % circeVersion,
      "io.circe"                   %% "circe-parser"             % circeVersion,
//      "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
//      "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,

      "org.reactivemongo" %% "reactivemongo" % "0.12.6",
//      "com.typesafe.play" % "play-json_2.11" % "2.4.0-M2",
      "ch.qos.logback" % "logback-classic" % "1.1.2",
      "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7",
      "org.julienrf" %% "reactivemongo-derived-codecs" % "3.0.0",


      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.0-RC1" % "test"
    )
  })
