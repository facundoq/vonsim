package vonsim.webapp
import vonsim.utils.CollectionUtils._
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scala.scalajs.js
import js.JSConverters._

import vonsim.assembly.LexerError
import scala.util.parsing.input.OffsetPosition
import vonsim.assembly.lexer.VonemuPosition
import vonsim.assembly.parser.Parser
import vonsim.assembly.lexer._
import scala.util.Random
import vonsim.assembly.Compiler
import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js.Dictionary
import com.scalawarrior.scalajs.ace._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.js.annotation.JSName
import vonsim.assembly.Location
import vonsim.assembly.ParserError

import scala.scalajs.js.timers.SetTimeoutHandle
import scala.scalajs.js.timers._
import vonsim.assembly.SemanticError
import vonsim.assembly.Compiler.FailedCompilation


abstract class VonSimUI {
  def root: HTMLElement

}

class MainUI(defaultCode: String) extends VonSimUI {
  println("Setting up UI..")
  val editorUI = new EditorUI(defaultCode,() => compile())
  val mainboardUI = new MainboardUI()
  val sim = div(id := "main",
    editorUI.root,
    mainboardUI.root).render

  val root = div(id := "pagewrap",
    header(
        div(id:="header",img(id := "icon", alt:="Von Sim Icon", title:="Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
           ,h1(id := "title", "a simplified intel 8088 simulator")
        )
    )
    ,sim).render
    
//  editorUI.root.onchange = (e: dom.Event) => compile()
//  editorUI.root.onkeyup = (e: dom.Event) => compile()
  
  root.onkeydown = (e: dom.KeyboardEvent) => {
    //println("Pressed " + e.keyCode + " " + e.ctrlKey)
    if ((e.ctrlKey || e.metaKey) && e.keyCode == 83) {
      e.preventDefault()
      compile()
    }

  }
  
  compile()
  
  def compile() {
    println("Compiling..")
    val s=editorUI.editor.getSession()
    val codeString = editorUI.editor.getValue()
    val compilation=Compiler(codeString)
    //mainboardUI.console.textContent=instructions.mkString("\n")
    compilation match {
      case Left(f) =>{
        println("Errors compiling") 
        val annotations=f.instructions.map(e => {
          e match{
            case Left(LexerError(l:Location,m:String)) => Annotation(l.line.toDouble-1,l.column.toDouble,m,"Lexer Error")
            case Left(ParserError(l:Location,m:String)) =>Annotation(l.line.toDouble-1,l.column.toDouble,m,"Parser Error")
            case Left(SemanticError(l:Location,m:String)) =>Annotation(l.line.toDouble-1,l.column.toDouble,m,"Semantic Error")
            case Right(x) => Annotation(x.line.toDouble-1,0.toDouble,x.instruction.toString(),"Correct Instruction")            
          }
          
        })
        val a =annotations.toJSArray//.map(_.asInstanceOf[Annotation])
        s.setAnnotations(a)
        
        println(f.instructions)
        val errors=f.instructions.lefts
        val errorLines= errors.map(_.location.line.toDouble-1).toJSArray
        val rows=s.getLength().toInt
        (0 until rows).foreach(l=> s.removeGutterDecoration(l, "ace_error "))
        errorLines.foreach(l=> s.addGutterDecoration(l, "ace_error "))
      }
      case Right(f) =>{ 
        println("Everything compiled fine")
        val annotations=f.instructions.map(e => { Annotation(e.line.toDouble-1,0.toDouble,e.instruction.toString(),"Correct Instruction")})
        s.setAnnotations(annotations.toJSArray)
        //TODO initialize Simulator
        
        (0 until s.getLength().toInt).foreach(row => s.removeGutterDecoration(row, "ace_error"))
      } 
    }    
    
  }

  def printLineNumbers(a: Array[Either[LexerError, List[Token]]]) {
    a.indices.foreach(i => {
      val eitherList = a(i)
      print(s"line ${i + 1}: ")
      if (eitherList.isRight) {
        val list = eitherList.right.get
        list.foreach(token => {
          print(token.pos + " " + token + ", ")
        })
      }
      println()
    })
  }

}

class ControlsUI() extends VonSimUI {
  val quickButton = button(
      img(src:="img/icons/quickrun.svg",alt:="Quick run")
      ,title:="F1: Reset simulator, load program into memory, run until cpu stops."
      ,id:="quickButton")
  val resetButton = button(
      img(src:="img/icons/loop2.svg",alt:="Reset")
      ,title:="F2: Reset cpu state and memory."
      ,id:="resetButton")
  val loadButton = button(
      img(src:="img/icons/download3.svg",alt:="Load")
      ,title:="F3: Load program into memory."
      ,id:="loadButton").render

  val runOneButton = button(img(src:="img/icons/step.svg",alt:="Step")
      ,title:="F4: Execute a single instruction."
      ,id:="runOneButton")
  val runPauseButton = button(img(src:="img/icons/play3.svg",alt:="Run")
      , title:="F5: Run program until cpu stops."
      ,id:="runPauseButton")
      
  val root = div(id := "controls"
    ,span(cls:="controlSection",quickButton)  
    ,span(cls:="controlSection",resetButton, loadButton, runOneButton,runPauseButton)
    ).render
}

class MainboardUI() extends VonSimUI {
  val cpuUI = new CpuUI()
  val memoryUI = new MemoryUI()
  val ioMemoryUI = new IOMemoryUI()
  val devicesUI = new DevicesUI()
  val controlsUI = new ControlsUI()

  controlsUI.loadButton.onclick = (e: dom.MouseEvent) => {} // TODO: run code here

  val console = pre("").render
  val consoleDir = div(id := "console",
    h2("Console"),
    console).render

  val root = div(id := "mainboard", controlsUI.root, div(id := "devices",
    cpuUI.root,
    memoryUI.root,
    ioMemoryUI.root, devicesUI.root)).render
}

class IOMemoryUI() extends VonSimUI {
  val memoryTable = table(
    thead(th("Name"), th("Address"), th("Value"))).render
  val r = new Random()
  val names = Map(10 -> "PA", 11 -> "PB", 12 -> "CA", 13 -> "CB")
  for (i <- 0 to 128) {
    val address = "%02X".format(i)
    val value = "%02X".format(r.nextInt(256))
    val name = names.getOrElse(i, "")
    memoryTable.appendChild(tr(td(name), td(address), td(value)).render)
  }

  val root = div(id := "iomemory", cls := "memory",
    div(cls := "flexcolumns",
      img(id := "iomemoryicon", src := "img/iconsets/bw/cable.png"), h2("IO Memory")),
    div(cls := "memoryTable", memoryTable)).render

}

class MemoryUI() extends VonSimUI {
  val memoryTable = table(
    thead(th("Address"), th("Value"))).render
  val r = new Random()
  for (i <- 0 to 192) {
    val address = "%04X".format(i)
    val value = "%02X".format(r.nextInt(256))
    memoryTable.appendChild(tr(td(address), td(value)).render)
  }

  val root = div(id := "memory", cls := "memory",
    div(cls := "flexcolumns",
      img(id := "memoryicon", src := "img/iconsets/bw/ram.png"), h2("Memory")),
    div(cls := "memoryTable", memoryTable)).render

}

class CpuUI() extends VonSimUI {
  val generalPurposeRegistersTable = table(cls := "registerTable ",
    thead(th("Register"), th(colspan := 2, "Value")),
    thead(th(""), th("H"), th("L")),
    tr(td("AX"), td("00"), td("00h")),
    tr(td("BX"), td("00"), td("00h")),
    tr(td("CX"), td("00"), td("00h")),
    tr(td("DX"), td("00"), td("00h"))).render

  val specialRegistersTable = table(cls := "registerTable ",
    thead(th("Register"), th("Value")),
    tr(td("PC"), td("0000h")),
    tr(td("SP"), td("4000h")) //      tr(td("Memory Address"),td("FAFEh")),
    //      tr(td("Current Instruction"),td("FEFAh"))
    ).render

  val bitTableA = table(cls := "bitTable", tr(td("0"), td("0"), td("0"), td("0"), td("0"), td("0"), td("0"), td("0")))
  val bitTableB = table(cls := "bitTable", tr(td("0"), td("0"), td("0"), td("0"), td("0"), td("0"), td("0"), td("0")))
  val resultBitTable = table(cls := "bitTable", tr(td("0"), td("0"), td("0"), td("0"), td("0"), td("0"), td("0"), td("0")))
  val operation = span("--")
  val alu = div(id := "alu", cls := "cpuElement",
    h3("ALU"),
    p("Operand A:", bitTableA),
    p("Operation:", operation),
    p("Operand B:", bitTableB),
    hr(),
    p("Result:", resultBitTable)).render

  val root = div(id := "cpu",
    div(cls := "flexcolumns",
      img(id := "cpuicon", src := "img/iconsets/bw/microchip.png"), h2("CPU")),
    div(cls := "cpuElement",
      h3("General Purpose Registers"),
      generalPurposeRegistersTable),
    div(cls := "cpuElement",
      h3("Special Registers"),
      specialRegistersTable),
    alu).render

}

class DevicesUI() extends VonSimUI {

  val root = div(id := "iomemory", cls := "memory",
    div(cls := "flexcolumns",
      img(id := "devicesicon", src := "img/iconsets/bw/printer.png"), h2("Devices")),
       "some pretty devices plz here").render

}

class EditorUI(defaultCode: String,onchange:() => Unit) extends VonSimUI {

  //document.body.appendChild(div(id:="aceEditor","asdasdasdasdasd").render)

  
  //val code: TextArea = textarea(cls := "textEditor").render
  //code.value = defaultCode
  val editor = webapp.myace.edit()
//  println(editor.container)
  editor.setTheme("ace/theme/monokai")
  editor.getSession().setMode("ace/mode/assembly_x86")
  editor.setValue(defaultCode)
  editor.getSession().setUseSoftTabs(true)
  editor.getSession().setUseWorker(false)
  editor.renderer.setShowGutter(true)
  
  
  val container=div(id:="aceEditor").render
  container.appendChild(editor.container)
  
  val root = div(id := "editor"
      ,container
    ).render
   
  var keystrokes = 0
  def keyTyped(){
      keystrokes+=1
//      println("keyTyped"+keystrokes)
      setTimeout(500)({act()})
  }
  
  def act(){
      keystrokes-=1
//      println("act"+keystrokes)
      if (keystrokes == 0){
//        println("onchanged"+keystrokes)
        onchange()
      }       
  }
  val eventListener: js.Function1[js.Any,js.Any]= (a:js.Any) => keyTyped().asInstanceOf[js.Any]
  
  editor.getSession().on("change", eventListener)
  
//  container.onkeydown = (e: dom.KeyboardEvent) => {
 //    println("keydown")
//    keyTyped()
//  }
  
  
}

