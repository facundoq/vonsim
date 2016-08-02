name := "vonemu"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "2.2.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"

libraryDependencies ++= Seq(
    "com.beachape" %% "enumeratum" % "1.4.8"
)