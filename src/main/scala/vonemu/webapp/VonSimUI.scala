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

}
class MainboardUI() extends VonSimUI {
  val cpuUI = new CpuUI()
  val memoryUI = new MemoryUI()
  val loadButton: Button = button("Load Program").render
  val resetButton: Button = button("Reset computer").render
  val runAllButton: Button = button("Run all").render
  val runOneButton: Button = button("Run one").render
  loadButton.onclick = (e: dom.MouseEvent) => {} // TODO: run code here
  
  val root= div(id:="mainboard",
              div(id:="controls",
                  loadButton,resetButton,runAllButton,runOneButton),
              div(id:="devices",
                cpuUI.root,
                memoryUI.root)
              ).render
}
class MemoryUI() extends VonSimUI {
  val memoryTable=table(cls:="memoryTable",
      thead(th("Address"),th("Value"))
      ).render
      val r=new Random()
      for (i <- 0 to 8192) {
        val address="%04X".format(i)
        val value="%02X".format(r.nextInt(256))
        memoryTable.appendChild(tr(td(address),td(value)).render)
      }
      
  val root= div(id := "memory", 
              h3("Memory",style:="text-align:center"),
              memoryTable).render
  
}
class CpuUI() extends VonSimUI {
  val generalPurposeRegistersTable=table(cls:="registerTable ",
      thead(th("Register"),th("Value (high)"),th("Value (low)")),
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
  
  val root= div(id := "cpu", 
      h3("CPU"),
      h4("General Purpose Registers"),
      generalPurposeRegistersTable,
      h4("Special Registers"),
      specialRegistersTable).render
      
}
class EditorUI(defaultCode:String) extends VonSimUI {
  
  
  val code: TextArea = textarea( cls:="textEditor").render
  
  val console: Element = pre("", style := "width=100%").render
  code.value = defaultCode
  code.onchange = (e: dom.Event) => compile()
  code.onkeyup = (e: dom.Event) => compile()

  

  val root=div(id := "editor",
      h2("Code"),
      div(code),
      h2("Console"),
      div(console)).render
  compile()

  def compile(){
    val codeString = code.value
    val instructions=Compiler(codeString)
    //console.textContent=instructions.mkString("\n")
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