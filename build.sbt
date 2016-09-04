name := """licenta-ocr-parser"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  ws,
  "log4j" % "log4j" % "1.2.17",
  "net.sourceforge.tess4j" % "tess4j" % "2.0.1"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)

import com.typesafe.sbt.packager.MappingsHelper._
mappings in Universal ++= directory(baseDirectory.value / "resources")

lazy val generate = taskKey[Unit]("Copy staging files")

generate := {
  println(s"Copying staging files to right location")
  Process("xcopy /E /Y .\\target\\universal\\stage C:\\Users\\darkg\\heroku-staging").lines foreach { l => println(s"copy> $l") }
}
