name := "chisel-module-template"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

libraryDependencies += "edu.berkeley.cs" % "chisel3_2.11" % "3.0-SNAPSHOT_2017-10-06"
libraryDependencies += "edu.berkeley.cs" % "chisel-iotesters_2.11" % "1.1-SNAPSHOT_2017-10-06"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.5",
  "org.scalacheck" %% "scalacheck" % "1.12.4")

