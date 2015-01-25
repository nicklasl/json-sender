name := """json-sender"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41" /*,
  cache,
  ws
   */
)
