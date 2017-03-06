enablePlugins(ScalaJSPlugin)

name := "VonEmu"

scalaVersion := "2.11.6"

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1"
libraryDependencies += "com.lihaoyi" %%% "scalatags" % "0.6.2"
libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
libraryDependencies += "org.scala-lang.modules" %%% "scala-parser-combinators" % "1.0.5"
