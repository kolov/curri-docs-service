import sbt.Keys._
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging

lazy val commonSettings = Seq(
  name := "curri-akka",
  organization := "curri",
  version := "1.0",
  scalaVersion := "2.11.8"
)




Revolver.settings

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

resolvers += "Typesafe" at "https://repo.typesafe.com/typesafe/releases/"

mainClass in(Compile, run) := Some("DocsServiceApp")
enablePlugins(sbtdocker.DockerPlugin)
enablePlugins(JavaAppPackaging)

imageNames in docker := Seq(
  // Sets the latest tag
  ImageName("kolov/service-docs:" + version.value)
)

dockerfile in docker := {
  // any vals to be declared here
  new sbtdocker.mutable.Dockerfile {
    from("kolov/java8")
    entryPoint("scripts/docs-service-app")

    user("root")

    add("target/universal/*.tgz", "/home/docker/app/")
    run("chown", "-R", "docker:docker", ".")
    println(artifact)

    user("docker")
    expose(9000)
  }
}

// from http://stackoverflow.com/questions/25144484/sbt-assembly-deduplication-found-error
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}


val pushDockerTask = TaskKey[Unit]("pushDocker", "Pushes docker file to local repo")
pushDockerTask := {
  import sys.process._
  val newVersion = "latest" //  or version.value
  Seq("docker", "tag", "kolov/service-docs:" + version.value,
    s"eu.gcr.io/iconic-setup-91510/curriculi-service-docs:$newVersion") !;
  Seq("docker", "push", s"eu.gcr.io/iconic-setup-91510/curriculi-service-docs:$newVersion") !;
}



lazy val mainProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= {
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
      "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7",


      "org.scalatest" %% "scalatest" % "3.0.1" % "test",
      "com.typesafe.akka" %% "akka-http-testkit" % akkaV % "test"
    )
  })
