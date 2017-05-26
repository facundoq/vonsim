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
import vonsim.simulator.SimulatorProgramLoaded
import vonsim.simulator.SimulatorProgramExecuting
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.SuccessfulCompilation
import vonsim.assembly.Compiler.SuccessfulCompilation
import com.scalawarrior.scalajs.ace.IEditSession
import vonsim.assembly.CompilationError
import vonsim.simulator.InstructionInfo
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.CompilationResult


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

class HeaderUI(s: Simulator) extends VonSimUI(s) {
  val controlsUI = new ControlsUI(s)
  
  
  val root=header(div(id := "header"
      , img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
      , h1(id := "title", "a simplified intel 8088 simulator (alpha version)"))
      , controlsUI.root
      ).render
  
  def update() {
    controlsUI.update()
  }
  
  def update(i:InstructionInfo) {
        controlsUI.update(i)
        
  }
  def compilationEvent(c:CompilationResult){
    controlsUI.compilationEvent(c)
  }
      
}

class MainUI(s: Simulator, defaultCode: String) extends VonSimUI(s) {
  println("Setting up UI..")
  var compilationResult:CompilationResult=null
  val editorUI = new EditorUI(s, defaultCode, () => compile())
 
  
  
  val mainboardUI = new MainboardUI(s)
  val headerUI=new HeaderUI(s)
  val sim = div(id := "main",
      editorUI.root,
    mainboardUI.root).render

  val root = div(id := "pagewrap"
      ,headerUI.root 
      ,sim).render


  root.onkeydown = (e: dom.KeyboardEvent) => {
    //println("Pressed " + e.keyCode + " " + e.ctrlKey)
    if ((e.ctrlKey || e.metaKey) && e.keyCode == 83) {
      e.preventDefault()
      compile()
      update()
    }
  } 
  
  headerUI.controlsUI.quickButton.onclick=(e:Any) =>{quickRun()}
  headerUI.controlsUI.loadButton.onclick=(e:Any) =>{loadProgram()}
  
  headerUI.controlsUI.resetButton.onclick=(e:Any) =>{reset()}
  headerUI.controlsUI.runPauseButton.onclick=(e:Any) =>{runInstructions()}
  headerUI.controlsUI.runOneButton.onclick=(e:Any) =>{stepInstruction()}
     

  println("Updating main stuff")
  update()
  compile()

  def compile() {
    println("Compiling..")
    val session = editorUI.editor.getSession()
    val codeString = editorUI.editor.getValue()
    compilationResult = Compiler(codeString)
    //mainboardUI.console.textContent=instructions.mkString("\n")
    clearGutterDecorations(session)
    compilationResult match {
      case Left(f) => {
        println("Errors compiling")
        
        val annotations=instructionsToAnnotations(f.instructions)
        val globalErrorAnnotations = f.globalErrors.map(e => Annotation(0, 0, e.msg, "global_error"))
        val a = (annotations ++ globalErrorAnnotations).toJSArray
        session.setAnnotations(a)

//        println(f.instructions)
        val errors = f.instructions.lefts
        val errorLines = errors.map(_.location.line.toDouble - 1).toJSArray
        errorLines.foreach(l => session.addGutterDecoration(l, "ace_error "))
        if (!f.globalErrors.isEmpty) {
          session.addGutterDecoration(0, "ace_error ")
        }
        

        
      }
      case Right(f) => {
        println("Succesful compilation")
        val annotations = f.instructions.map(e => { Annotation(e.line.toDouble - 1, 0.toDouble, e.instruction.toString(), "Correct Instruction") })
        val warningAnnotations = f.warnings.map(w => { Annotation(w._1.toDouble - 1, 0.toDouble, w._2, "Warning") })
        
        session.setAnnotations((annotations++warningAnnotations).toJSArray)
        warningAnnotations.indices.foreach(i => session.addGutterDecoration(i.toDouble-1, "ace_warning"))
        
      }
    }
    headerUI.compilationEvent(compilationResult)
  }
  def instructionsToAnnotations(instructions:List[Either[CompilationError,InstructionInfo]])={
    instructions.map(e => {
          e match {
            case Left(LexerError(l: Location, m: String))    => Annotation(l.line.toDouble - 1, l.column.toDouble, m, "Lexer Error")
            case Left(ParserError(l: Location, m: String))   => Annotation(l.line.toDouble - 1, l.column.toDouble, m, "Parser Error")
            case Left(SemanticError(l: Location, m: String)) => Annotation(l.line.toDouble - 1, l.column.toDouble, m, "Semantic Error")
            case Right(x)                                    => Annotation(x.line.toDouble - 1, 0.toDouble, x.instruction.toString(), "Correct Instruction")
          }
        })
  }
  def clearGutterDecorations(session:IEditSession){
    (0 until session.getLength().toInt).foreach(row => session.removeGutterDecoration(row, "ace_error"))
    (0 until session.getLength().toInt).foreach(row => session.removeGutterDecoration(row, "ace_warning"))
  }

  def update(){
    println("Updating UI... ")
    editorUI.update()
    mainboardUI.update()
  }
  def update(i:InstructionInfo) {
    println(s"Updating UI for instruction $i")
    editorUI.update(i)
    mainboardUI.update(i)
  }
  def reset(){
    println("Resetting	... ")
   s.reset()
   update()
  }
  def runInstructions(){
    println("Running... ")
     editorUI.disable()
     val instructions=s.runInstructions()
     update()
     if (instructions.length>0 && instructions.last.isLeft){
       val error = instructions.last.left.get
       executionError(error)
     }
  }
  def executionError(message:String){
    dom.window.alert(s"Execution error: $message")
  }

  def stepInstruction(){
     println("Step instruction.. ")
     val i=s.stepInstruction()
     i match{
       case Left(message) => executionError(message)
       case Right(i) => update(i)
     }
     
    
  }
  def quickRun(){
    compilationResult match {
       case Right(c:SuccessfulCompilation) => {
//          editorUI.disable()
//           println("Loading program... ")
//           s.load(c)
//           println("Running program... ")
//           val instructions= s.runInstructions()
//           update()
//           if (instructions.length>0 && instructions.last.isLeft){
//               val error = instructions.last.left.get
//               executionError(error)
//             }
//           println("Done")
          loadProgram()
          runInstructions()
       }
       case _ => dom.window.alert("Compilation failed, can't run program")
     }
   
    
  }
  def loadProgram(){
    compilationResult match {
       case Right(c:SuccessfulCompilation) => {
         println("Loading program... ")
        s.load(c)
        update()
        println("Done")
       }
       case _ => dom.window.alert("Compilation failed, can't load program")
     }
    
  }

}



