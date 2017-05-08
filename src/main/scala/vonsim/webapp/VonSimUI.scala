package vonsim.webapp
import vonsim.utils.CollectionUtils._
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scala.scalajs.js
import js.JSConverters._

import scala.collection.mutable


import scala.util.parsing.input.OffsetPosition
import vonsim.assembly.lexer.VonemuPosition



import vonsim.assembly.Compiler
import org.scalajs.dom.raw.HTMLElement



import vonsim.assembly.Location
import vonsim.assembly.LexerError
import vonsim.assembly.ParserError
import vonsim.assembly.SemanticError
import vonsim.assembly.Compiler.FailedCompilation

import vonsim.simulator.Simulator
import vonsim.simulator.InstructionInfo
import vonsim.simulator.DWord
import vonsim.simulator.Word
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.SuccessfulCompilation

abstract class HTMLUI {
  def root: HTMLElement
}

abstract class VonSimUI(val s: Simulator) extends HTMLUI{

  def update() // update changes made to the simulator
  def update(i:InstructionInfo) // update UI after execution of instruction
  
  def formatIOAddress(a:Int)={
    "%02X".format(a)
  }
  def formatAddress(a:Int)={
    "%04X".format(a)
  }
  def formatWord(a:Word)={
    "%02X".format(a.toUnsignedInt)
  }
  def formatDWord(a:DWord)={
    "%04X".format(a.toUnsignedInt)
  }
}


class MainUI(s: Simulator, defaultCode: String) extends VonSimUI(s) {
  println("Setting up UI..")
  var compilationResult:CompilationResult=null
  val editorUI = new EditorUI(s, defaultCode, () => compile())
  val mainboardUI = new MainboardUI(s)
  val sim = div(id := "main",
    editorUI.root,
    mainboardUI.root).render

  val root = div(id := "pagewrap",
    header(
      div(id := "header", img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png"), h1(id := "title", "a simplified intel 8088 simulator (alpha version)"))), sim).render

  //  editorUI.root.onchange = (e: dom.Event) => compile()
  //  editorUI.root.onkeyup = (e: dom.Event) => compile()

  root.onkeydown = (e: dom.KeyboardEvent) => {
    //println("Pressed " + e.keyCode + " " + e.ctrlKey)
    if ((e.ctrlKey || e.metaKey) && e.keyCode == 83) {
      e.preventDefault()
      update()
      compile()
    }
  } 
  
  mainboardUI.controlsUI.quickButton.onclick=(e:Any) =>{
     compilationResult match {
       case Right(c:SuccessfulCompilation) => {
         println("Loading program... ")
         s.load(c)
         println("Running program... ")
         s.run()
         println("Updating UI..")
         update()
         println("Done")
       }
       case _ => println("Compilation failed, can't run program")
     }
  } 
     

  println("Updating main stuff")
  update()
  compile()

  def compile() {
    println("Compiling..")
    val s = editorUI.editor.getSession()
    val codeString = editorUI.editor.getValue()
    compilationResult = Compiler(codeString)
    //mainboardUI.console.textContent=instructions.mkString("\n")
    compilationResult match {
      case Left(f) => {
        println("Errors compiling")
        val annotations = f.instructions.map(e => {
          e match {
            case Left(LexerError(l: Location, m: String))    => Annotation(l.line.toDouble - 1, l.column.toDouble, m, "Lexer Error")
            case Left(ParserError(l: Location, m: String))   => Annotation(l.line.toDouble - 1, l.column.toDouble, m, "Parser Error")
            case Left(SemanticError(l: Location, m: String)) => Annotation(l.line.toDouble - 1, l.column.toDouble, m, "Semantic Error")
            case Right(x)                                    => Annotation(x.line.toDouble - 1, 0.toDouble, x.instruction.toString(), "Correct Instruction")
          }

        })
        val globalErrorAnnotations = f.globalErrors.map(e => Annotation(0, 0, e.msg, "global_error"))
        val a = (annotations ++ globalErrorAnnotations).toJSArray
        s.setAnnotations(a)

//        println(f.instructions)
        val errors = f.instructions.lefts
        val errorLines = errors.map(_.location.line.toDouble - 1).toJSArray
        val rows = s.getLength().toInt
        (0 until rows).foreach(l => s.removeGutterDecoration(l, "ace_error "))
        errorLines.foreach(l => s.addGutterDecoration(l, "ace_error "))
        if (!f.globalErrors.isEmpty) {
          s.addGutterDecoration(0, "ace_error ")
        }

      }
      case Right(f) => {
        println("Succesfull compilation")
        val annotations = f.instructions.map(e => { Annotation(e.line.toDouble - 1, 0.toDouble, e.instruction.toString(), "Correct Instruction") })
        s.setAnnotations(annotations.toJSArray)

        (0 until s.getLength().toInt).foreach(row => s.removeGutterDecoration(row, "ace_error"))
      }
    }
  }

  def update() {
    editorUI.update()
    mainboardUI.update()
  }
  def update(i:InstructionInfo) {
    editorUI.update(i)
    mainboardUI.update(i)
  }

}



