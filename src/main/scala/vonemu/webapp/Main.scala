package mazes.webapp

// tutorial https://www.scala-js.org/tutorial/basic/
// canvas https://github.com/vmunier/scalajs-simple-canvas-game/blob/master/src/main/scala/simplegame/SimpleCanvasGame.scala

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom.Element

import dom.document
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import vonemu.parsercombinators.compiler.WorkflowCompiler
import vonemu.assembly.Lexer
import java.awt.Event
import scala.util.parsing.input.Position
import vonemu.assembly.Token
import vonemu.assembly.LexerError
import scala.util.parsing.input.OffsetPosition
import vonemu.assembly.VonemuPosition
import vonemu.assembly.Parser
import vonemu.assembly.EMPTY

object Main extends JSApp {

  def main(): Unit = {
    val ui = new EditorUI(document.body)
    ui.setupUI()
    //document.body.appendChild(pre(gencode).render)
  }

  def gencode() = {
    val r = "a b c d".split(" ").toList
    val rx = r.map(_ + "x")
    val rh = r.map(_ + "l")
    val rl = r.map(_ + "h")
    val ins = "end add nop ret org mov".split(" ").toList
    val all = rx ++ rh ++ rl ++ ins

    val lexerdefs = all map { r => s"""def $r = positioned { "$r" ^^ (_ => ${r.toUpperCase}()) }""" }
    val lexerdefsstr = lexerdefs.foldLeft("")((a, b) => a + "\n" + b)
    val lexeritems = all.foldLeft("")((a, b) => a + " | " + b) + "\n"

    lexeritems ++ lexerdefsstr
  }

}

class EditorUI(base: HTMLElement) {

  var console: Element = null
  var compileButton: Button = null
  var code: TextArea = null

  def defaultCode =
"""mov ax, bx
MOV ax, bx
mov  AX, bX
   mov  ax, bx
mov  ax, bx   
mov     ax, bx
mov  ax   , bx
mov  ax,bx
hola: mov ax, bx
mov [bx],ax
mov ax, 2
mov ax, -25
mov ax, 25AH
mov ax, 25Ah
mov ax, 10001111B
not ax
add ax, bx
add ax, 3
add ax, 26h
adc ax, 26h
xor ax, 26h
cmp ax, 26h
mov ax,sp
in al,PIC
in al,123
out ax,dx
jc hola
org 1000


JMP HOLA
JC HOLA
CALL HOLA
RET
NOP
HLT
END
CLI
StI
ret
pushf
popf
push Ax
pop bx
pop CX
int 4
"""

  def setupUI() = {
    code = textarea(width := "50%", rows := 10).render
    code.value = defaultCode
    code.onchange = (e: dom.Event) => compile()
    code.onkeyup = (e: dom.Event) => compile()
    compileButton = button("Compile").render
    compileButton.onclick = (e: dom.MouseEvent) => compile()

    console = pre("", style := "width=100%").render

    this.base.appendChild(
      div(
        h1("VonEmu"),
        p("Code"),
        div(code),
        div(compileButton),
        p("Console"),
        div(console)).render)
    compile()
  }

  def compile(): Unit = {
    val codeString = code.value
    val instructions = codeString.split("\n")
    var optionTokens = instructions map { Lexer(_) }
    val fixedTokens=Lexer.fixLineNumbers(optionTokens)
    console.textContent = "Tokens:\n"
    console.textContent += fixedTokens.map(_.toString()).mkString("\n")
    
    if (fixedTokens.count(_.isRight)==fixedTokens.length){
      console.textContent += "\nAST:\n"
      val tokens= (fixedTokens map {_.right.get}) filter {t => !(t.length == 1 && t(0)==EMPTY())}
      
      val asts= tokens map {Parser(_)}
      console.textContent += asts.mkString("\n")  
    }
    
    
    //printLineNumbers(fixedResult)

  }
  def printLineNumbers(a: Array[Either[LexerError, List[Token]]]) {
    a.indices.foreach(i => {
      val eitherList = a(i)
      print(s"line ${i+1}: ")
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
