package vonsim.webapp

import vonsim.simulator.Simulator

import org.scalajs.dom.html._
import scalatags.JsDom.all._
import vonsim.simulator._
import vonsim.assembly.Compiler.CompilationResult

class ControlsUI(s: Simulator) extends VonSimUI(s) {
  

      val quickButton = button(
    img(src := "img/icons/quickrun.svg", alt := "Quick run")
    ,"Quick run"
    ,title := "F1: Reset simulator, load program into memory, run until cpu stops." 
    ,id := "quickButton").render


  val loadButton = button(
    img(src := "img/icons/download3.svg", alt := "Load"),
    "Load program",
    title := "F2: Load program into memory without starting execution.", id := "loadButton").render

  val resetButton = button(
    img(src := "img/icons/loop2.svg", alt := "Reset")
    ,"Reset"
    ,title := "F3: Reset cpu state to repeat the execution.", id := "resetButton").render
  val runPauseButton = button(img(src := "img/icons/play3.svg", alt := "Run"),
      "Run",
      title := "F5: Run program until cpu stops.", id := "runPauseButton").render
      
    val runOneButton = button(img(src := "img/icons/step.svg", alt := "Step"),
      "Step",  
      title := "F6: Execute a single instruction.", id := "runOneButton").render
      
 


  val root = span(id := "controls"
      ,span(cls := "controlSection", quickButton)
      ,span(cls := "controlSection", loadButton)
     ,span(cls := "controlSection",resetButton)
     ,span(cls := "controlSection",runOneButton)
     ,span(cls := "controlSection",runPauseButton)
      ).render

  def succesfullCompilation(){
    loadButton.disabled=false
      quickButton.disabled=false
  }
  def failedCompilation(){
    loadButton.disabled=true
    quickButton.disabled=true
  }
  def compilationEvent(c:CompilationResult){
    
  }
  def update() {
    
    s.state  match{
      
      case SimulatorNoProgramLoaded => {
        resetButton.disabled=true
        runPauseButton.disabled=true
        runOneButton.disabled=true
      }
      case SimulatorProgramExecuting => {
        resetButton.disabled=false
        runPauseButton.disabled=false
        runOneButton.disabled=false
      }
      case SimulatorProgramLoaded => {
        resetButton.disabled=true
        runPauseButton.disabled=false
        runOneButton.disabled=false
      }
      case ( SimulatorExecutionFinished | SimulatorExecutionError(_)) => {
        resetButton.disabled=false
        runPauseButton.disabled=true
        runOneButton.disabled=true
      }
    }
  }
  def update(i:InstructionInfo) {
    
  }
}
