name := "vonemu"
/*** To include dependencies for eclipse: reload
eclipse with-source=true
 */
 
version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0" % "test"

libraryDependencies ++= Seq(
    "com.beachape" %% "enumeratum" % "1.4.10"
)


libraryDependencies ++= Seq(
    "com.beachape" %% "enumeratum" % "1.4.8"
)