package vonsim.webapp
import vonsim.simulator._
import scalatags.JsDom.all._
import vonsim.assembly.Compiler.CompilationResult

class HeaderUI(s: Simulator) extends VonSimUI(s) {
  val controlsUI = new ControlsUI(s)
  
  
  val root=header(div(id := "header"
      , img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
      , controlsUI.root
      )).render
  
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