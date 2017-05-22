package vonsim.webapp
import vonsim.simulator.InstructionInfo
import com.scalawarrior.scalajs.ace._
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.js.annotation.JSName
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scala.scalajs.js
import js.JSConverters._
import vonsim.simulator.Simulator
import scalatags.JsDom.all._
import scala.scalajs.js.timers.SetTimeoutHandle
import scala.scalajs.js.timers._

import vonsim.webapp

class EditorUI(s: Simulator, defaultCode: String, onchange: () => Unit) extends VonSimUI(s) {

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

  val container = div(id := "aceEditor").render
  container.appendChild(editor.container)

  val root = div(id := "editor", container).render

  editor.getSession().on("change", new DelayedJSEvent(onchange).listener)

  //  container.onkeydown = (e: dom.KeyboardEvent) => {
  //    println("keydown")
  //    keyTyped()
  //  }
  
  def update() {
    //TODO
  }
  def update(i:InstructionInfo) {
    // TODO improve
    update()
  }
}

class DelayedJSEvent(val response: () => Unit) {
  var keystrokes = 0
  val listener: js.Function1[js.Any, js.Any] = (a: js.Any) => keyTyped().asInstanceOf[js.Any]

  def keyTyped() {
    keystrokes += 1
    //      println("keyTyped"+keystrokes)
    setTimeout(500)({ act() })
  }

  def act() {
    keystrokes -= 1
    //      println("act"+keystrokes)
    if (keystrokes == 0) {
      //        println("onchanged"+keystrokes)
      response()
    }
  }

}