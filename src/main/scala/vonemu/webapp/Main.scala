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

object Main extends JSApp {

  def main(): Unit = {
    val ui = new EditorUI(document.body)
    ui.setupUI()
    //document.body.appendChild(pre(gencode).render)
  }
  
  def gencode()={
  val r="a b c d".split(" ").toList
  val rx=r.map(_+"x")
  val rh=r.map(_+"l")
  val rl=r.map(_+"h")
  val ins="end add nop ret org mov".split(" ").toList
  val all=rx++rh++rl++ins
  
  val lexerdefs=all map { r => s"""def $r = positioned { "$r" ^^ (_ => ${r.toUpperCase}()) }"""}
  val lexerdefsstr=lexerdefs.foldLeft("")((a,b) => a+"\n"+b)
  val lexeritems=all.foldLeft("")((a,b) => a+" | "+b)+"\n"
  
  lexeritems++lexerdefsstr
  }

}


class EditorUI(base: HTMLElement) {

  var console: Element = null
  var compileButton: Button = null
  var code: TextArea = null

  def defaultCode = "mov ax, bx \nmov ax, 2"

  def setupUI() = {
    code = textarea(width := "50%", rows := 10).render
    code.value = defaultCode
    code.onchange = (e: dom.Event) => compile()
    code.onkeyup = (e: dom.Event) => compile()
    compileButton = button("Compile").render
    compileButton.onclick = (e: dom.MouseEvent) => compile()

    console = pre("",style:="width=100%").render
    
    
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
    val result=Lexer(codeString)
    console.textContent = result.toString()
  }

}
