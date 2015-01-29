import _root_.spray.revolver.RevolverPlugin.Revolver
import sbt.Keys._

name := "seeker-akka"

version := "1.0"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-deprecation", "-feature")

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= {
  val akkaVersion = "2.3.7"
  val sprayVersion = "1.3.1"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % "1.0-M1",
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,
    "io.spray" %% "spray-json" % sprayVersion,
    "io.spray" %% "spray-testkit" % sprayVersion % "test",
    "org.json4s" %% "json4s-native" % "3.2.11",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "nu.validator.htmlparser" % "htmlparser" % "1.4"
  )
}

updateOptions := updateOptions.value.withCachedResolution(true)

Revolver.settings

javaOptions in Revolver.reStart += "-Dfile.encoding=utf8"
    