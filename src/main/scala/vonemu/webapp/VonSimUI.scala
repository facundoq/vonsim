package vonemu.webapp

import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom

import vonemu.assembly.LexerError
import scala.util.parsing.input.OffsetPosition
import vonemu.assembly.lexer.VonemuPosition
import vonemu.assembly.parser.Parser
import vonemu.assembly.lexer._
import scala.util.Random
import vonemu.assembly.Compiler
import org.scalajs.dom.raw.HTMLElement
import scala.scalajs.js.Dictionary

abstract class VonSimUI {
  def root:HTMLElement
  
}

class MainUI(defaultCode:String) extends VonSimUI {
  
  
  val editorUI = new EditorUI(defaultCode)
  val mainboardUI=new MainboardUI()
  val sim= div(id := "main",
              editorUI.root,
              mainboardUI.root
              ).render
  
  val root=div(id:="pagewrap",
      header(img(id:="icon",src:="img/icon2.png"),
          h1(id:="title","a simplified intel 8088 simulator")),
          sim
      ).render
      
  editorUI.code.onchange = (e: dom.Event) => compile()
  editorUI.code.onkeyup = (e: dom.Event) => compile()
  
  root.onkeydown = (e:dom.KeyboardEvent) =>{
    println("Pressed "+e.keyCode+" "+e.ctrlKey)
    if ((e.ctrlKey || e.metaKey) && e.keyCode == 83){
      e.preventDefault()
    }
    
  }
  
  def compile(){
    val codeString = editorUI.code.value
//    val instructions=Compiler(codeString)
    //mainboardUI.console.textContent=instructions.mkString("\n")
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
  val loadButton: Button = button("Load Program").render
  val resetButton: Button = button("Reset computer").render
  val runAllButton: Button = button("Run all").render
  val runOneButton: Button = button("Run one").render
  val root=div(id:="controls",
                  loadButton,resetButton,runAllButton,runOneButton).render
}
class MainboardUI() extends VonSimUI {
  val cpuUI = new CpuUI()
  val memoryUI = new MemoryUI()
  val ioMemoryUI = new IOMemoryUI()
  val devicesUI= new DevicesUI()
  val controlsUI= new ControlsUI()
  
  
  controlsUI.loadButton.onclick = (e: dom.MouseEvent) => {} // TODO: run code here
  
  val console= pre("").render
  val consoleDir=div(id:="console",
                  h2("Console"),
                  console).render
  
  val root= div(id:="mainboard"
              ,controlsUI.root
              ,div(id:="devices",
                cpuUI.root,
                memoryUI.root,
                ioMemoryUI.root
                ,devicesUI.root)            

              ).render
}

class IOMemoryUI() extends VonSimUI {
  val memoryTable= table(
      thead(th("Name"),th("Address"),th("Value"))
      ).render
      val r=new Random()
      val names= Map(10 -> "PA", 11 -> "PB", 12-> "CA", 13 -> "CB")
      for (i <- 0 to 128) {
        val address="%02X".format(i)
        val value="%02X".format(r.nextInt(256))
        val name = names.getOrElse(i, "")
        memoryTable.appendChild(tr(td(name),td(address),td(value)).render)
      }
      
  val root= div(id := "iomemory", cls:="memory", 
              div(cls:="flexcolumns",
                img(id:="iomemoryicon",src:="img/iconsets/bw/cable.png")
                ,h2("IO Memory")
               ),
              div(cls:="memoryTable",memoryTable)
              ).render
  
}

class MemoryUI() extends VonSimUI {
  val memoryTable= table(
      thead(th("Address"),th("Value"))
      ).render
      val r=new Random()
      for (i <- 0 to 8192) {
        val address="%04X".format(i)
        val value="%02X".format(r.nextInt(256))
        memoryTable.appendChild(tr(td(address),td(value)).render)
      }
      
  val root= div(id := "memory", cls:="memory",
              div(cls:="flexcolumns",
                img(id:="memoryicon",src:="img/iconsets/bw/ram.png")
                ,h2("Memory")
               ),
              div(cls:="memoryTable",memoryTable)
              ).render
  
}

class CpuUI() extends VonSimUI {
  val generalPurposeRegistersTable=table(cls:="registerTable ",
      thead(th("Register"),th(colspan:=2,"Value")),
      thead(th(""),th("H"),th("L")),
      tr(td("AX"),td("00"),td("00h")),
      tr(td("BX"),td("00"),td("00h")),
      tr(td("CX"),td("00"),td("00h")),
      tr(td("DX"),td("00"),td("00h"))
      ).render
      
  val specialRegistersTable=table(cls:="registerTable ",
      thead(th("Register"),th("Value")),
      tr(td("PC"),td("0000h")),
      tr(td("SP"),td("4000h"))
//      tr(td("Memory Address"),td("FAFEh")),
//      tr(td("Current Instruction"),td("FEFAh"))
      ).render
      
  val bitTableA=table(cls:="bitTable",tr(td("0"),td("0"),td("0"),td("0"),td("0"),td("0"),td("0"),td("0")))
  val bitTableB=table(cls:="bitTable",tr(td("0"),td("0"),td("0"),td("0"),td("0"),td("0"),td("0"),td("0")))
  val resultBitTable=table(cls:="bitTable",tr(td("0"),td("0"),td("0"),td("0"),td("0"),td("0"),td("0"),td("0")))
  val operation=span("--")
  val alu=div(id:="alu",cls:="cpuElement",
      h3("ALU"),
      p("Operand A:",bitTableA),
      p("Operation:",operation),
      p("Operand B:",bitTableB),
      hr(),
      p("Result:",resultBitTable)
      ).render
  
  val root= div(id := "cpu", 
      div(cls:="flexcolumns",
          img(id:="cpuicon",src:="img/iconsets/bw/microchip.png")
          ,h2("CPU")
         ),
      div( cls:="cpuElement",
        h3("General Purpose Registers"),
        generalPurposeRegistersTable
        ),
      div( cls:="cpuElement",
        h3("Special Registers"),
        specialRegistersTable
        ),
        alu
      ).render

}

class DevicesUI() extends VonSimUI {

      
  val root= div(id := "iomemory", cls:="memory", 
              div(cls:="flexcolumns",
                img(id:="devicesicon",src:="img/iconsets/bw/printer.png")
                ,h2("Devices")
               ),
              div(cls:="memoryTable","asd")
              ).render
  
}


class EditorUI(defaultCode:String) extends VonSimUI {
  
  
  val code: TextArea = textarea( cls:="textEditor").render
  code.value = defaultCode

  val root=div(id := "editor",
      //h2("Code"),
      div(code)).render
}