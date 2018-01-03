import play.PlayImport.PlayKeys._

name := "viedemerde-rest-api"

version := "0.1.0"

scalaVersion := "2.11.6"

libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "0.11.0.play24"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalatestplus" %% "play" % "1.4.0-M3" % "test"
)

routesGenerator := InjectedRoutesGenerator

lazy val root = (project in file(".")).enablePlugins(PlayScala)
