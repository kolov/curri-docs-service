import com.typesafe.sbt.SbtAspectj._

name := "curri-akka"
organization := "curri"

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= {
  val akkaV = "2.4.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaV,

    "org.reactivemongo" %% "reactivemongo" % "0.10.5.0.akka23",
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
    "com.typesafe.play" % "play-json_2.11" % "2.4.0-M2",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.netflix.eureka" % "eureka-client" % "1.4.12",

    "org.scalatest" %% "scalatest" % "3.0.1" % "test",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test"
  )
}
Revolver.settings

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"


mainClass in(Compile, run) := Some("Boot")
enablePlugins(sbtdocker.DockerPlugin)


dockerfile in docker := {
  // any vals to be declared here
  new sbtdocker.mutable.Dockerfile {
    from("frolvlad/alpine-oraclejdk8:slim")
    volume("/app")
    val artifact: File = assembly.value
    val artifactTargetPath = s"/app/${artifact.name}"

    add(artifact, artifactTargetPath)
    expose(9000)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}

// from http://stackoverflow.com/questions/25144484/sbt-assembly-deduplication-found-error
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}
