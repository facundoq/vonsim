package vonsim.webapp
import vonsim.simulator._
import scalatags.JsDom.all._
import vonsim.assembly.Compiler.CompilationResult

class HeaderUI(s: VonSimState) extends VonSimUI(s) {
  val controlsUI = new ControlsUI(s)
  val simulatorStateUI= new SimulatorStateUI(s)
  
  
  val root=header(div(id := "header"
      , img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
      , controlsUI.root
      ,span(cls:="controlSectionSeparator")
      , simulatorStateUI.root
      )).render
  
  def simulatorEvent() {
    controlsUI.simulatorEvent()
    simulatorStateUI.simulatorEvent()
  }
  
  def simulatorEvent(i:InstructionInfo) {
        controlsUI.simulatorEvent(i)
        simulatorStateUI.simulatorEvent(i)
  }
  def compilationEvent(){
    controlsUI.compilationEvent()
    simulatorStateUI.compilationEvent()
  }
  
      
}
