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
import vonsim.simulator.SimulatorProgramExecuting

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
  
  val editorControlsUI= new EditorControlsUI(s)
  
  val container = div(id := "aceEditor"
      ,editorControlsUI.root
      ,editor.container).render
  

  val root = div(id := "editor", container).render
  
  

  editor.getSession().on("change", new DelayedJSEvent(onchange).listener)

  //  container.onkeydown = (e: dom.KeyboardEvent) => {
  //    println("keydown")
  //    keyTyped()
  //  }
  
  def update() {
    // TODO check if code can be run and if the cpu is halted to allow enable buttons
    
    if (s.state == SimulatorProgramExecuting){
      disable()
    }else{
      enable()
    }
    
  }
  def update(i:InstructionInfo) {
    // TODO improve
    update()
  }
  
  def enable(){
    container.disabled=false
  }
  def disable(){
    container.disabled=true
  }
}

class EditorControlsUI(s: Simulator) extends VonSimUI(s) {
  val quickButton = button(
    img(src := "img/icons/quickrun.svg", alt := "Quick run")
    ,"Quick run"
    ,title := "F1: Reset simulator, load program into memory, run until cpu stops." 
    ,id := "quickButton").render
//  quickButton.onclick = (e:dom.MouseEvent) => {println("hola")}

  val loadButton = button(
    img(src := "img/icons/download3.svg", alt := "Load"),
    "Load program",
    title := "F2: Load program into memory without starting execution.", id := "loadButton").render
  val root = div(id := "editorControls" 
      ,span(cls := "controlSection", quickButton)
      ,span(cls := "controlSection", loadButton) 
      ).render
  
  def update() {
    
    
  }
  def update(i:InstructionInfo) {
    
  }
  
  def enable(){
    loadButton.disabled=false
      quickButton.disabled=false
  }
  def disable(){
    loadButton.disabled=true
    quickButton.disabled=true
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