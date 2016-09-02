name := """licenta-ocr-parser"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "org.jscience" % "jscience" % "4.3.1",
    ws,
  "de.lmu.ifi.dbs.jfeaturelib" % "JFeatureLib" % "1.6.3" from "https://oss.sonatype.org/content/repositories/releases/de/lmu/ifi/dbs/jfeaturelib/JFeatureLib/1.6.3/JFeatureLib-1.6.3.jar"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _ )


lazy val generate = taskKey[Unit]("Copy staging files")

generate := {
  println(s"Copying staging files to right location")
  Process("xcopy /E /Y .\\target\\universal\\stage C:\\Users\\darkg\\heroku-staging").lines foreach { l => println(s"copy> $l") }
}
