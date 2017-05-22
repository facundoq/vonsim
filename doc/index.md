# VonSim project structure

VonSim consists of three separate parts:

* A Compiler, that takes a String and produces Instructions and additional info ready to be loaded into the Simulator. This part depends only on the Simulator, and is found in package `vonsim.assembly`
* A Simulator, that loads a compiled program into memory and executes it. The simulator's code is in package `vonsim.simulator`
* A web app, that contains a code editor and an UI that shows the state of the simulator, and allows the user to write a program, compile it, load it into the simulator, and watch and control the execution. This part depends on both the Simulator and the Compiler. The app's code is in package `vonsim.webapp`. 


All the code is written in Scala, but compiles to Javascript via the excelent [ScalaJS project.](https://www.scala-js.org/). ScalaJS supports all Scala constructs except some types of reflection, produces fast and compact code (though it's not very readable), and is really easy to use.

The webapp uses the [Ace Editor](https://ace.c9.io/) as a code editor and [Clusterize.js](https://github.com/NeXTs/Clusterize.js/) to display the memory as a big table.

The Compiler uses Scala's [Parser Combinators API](https://github.com/scala/scala-parser-combinators) to parse the assembly code.

The project is built via [SBT](http://www.scala-sbt.org/), the standard Scala build tool, using the [ScalaJS sbt plugin](http://www.scala-js.org/doc/sbt-plugin.html).

Tests are written with [ScalaTest for ScalaJS](http://www.scalatest.org/user_guide/using_scalajs) and run via sbt.

## Compiler

## Simulator

## Webapp
