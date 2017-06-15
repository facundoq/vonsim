package vonsim.webapp

import vonsim.simulator.Simulator

import org.scalajs.dom.html._
import scalatags.JsDom.all._
import vonsim.simulator._
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.FailedCompilation


class SimulatorStateUI(s: VonSimState) extends VonSimUI(s) {
  def stateToIcon(state:SimulatorState)= state match{
    case SimulatorExecutionError(msg) => "exclamation-circle"
    case SimulatorExecutionFinished => "check-circle"
    case SimulatorExecutionStopped => "circle"
    case SimulatorProgramExecuting => "pause-circle"      
  }
  def stateToButtonClass(state:SimulatorState)= state match{
    case SimulatorExecutionError(msg) => "btn-danger"
    case SimulatorExecutionFinished => "btn-success"
    case SimulatorExecutionStopped => "btn-danger"
    case SimulatorProgramExecuting => "btn-warning"      
  }
  def stateToMessage(state:SimulatorState)= state match{
    case SimulatorExecutionError(msg) => "Execution Error"
    case SimulatorExecutionFinished => "Execution Finished"
    case SimulatorExecutionStopped => "No Program Loaded"
    case SimulatorProgramExecuting => "Program executing"      
  }
  def stateToTooltip(state:SimulatorState)= state match{
    case SimulatorExecutionError(error) => error.message
    case SimulatorExecutionFinished => "The execution has finished or has been stopped. Reload the program to execute again or perform a Quick Run."
    case SimulatorExecutionStopped => "There is no program loaded in the simulator; you must load one before executing, or perform a Quick Run"
    case SimulatorProgramExecuting => "The program is executing. You can execute instructions one at a time with Step or until the program finishes with Run. While the program is running you cannot modify the code in the editor"      
  }
  
  val stateIcon=i(cls:="").render
  val stateTitle=span().render
  val root=span(cls:=""
      ,stateIcon
      ,stateTitle
      ).render
  simulatorEvent()
  def simulatorEvent() {
    val color=stateToButtonClass(s.s.state)
    root.className="btn "+color+" simulatorState"
    root.title=stateToTooltip(s.s.state)
    stateTitle.textContent=stateToMessage(s.s.state)
    stateIcon.className="fa fa-"+stateToIcon(s.s.state)
  }
  def simulatorEvent(i: InstructionInfo) {
    simulatorEvent()
  }
  def compilationEvent(){
    
  }
      
      
}
class ControlsUI(s: VonSimState) extends VonSimUI(s) {
    //http://fontawesome.io/icons/
    class LoadOrStopButton(s:VonSimState) extends VonSimUI(s){
      val hiddenClass="hidden"
      val loadButton = buttonFactory("Debug","F2: Load program into memory without starting execution.","fa-bug")
      val stopButton = buttonFactory("Cancel", "F3: Leave step mode.", "fa-stop")
      stateLoad()
      val root=span(
          loadButton,
          stopButton  
          ).render
          
      def stateLoad(){
        stopButton.classList.add(hiddenClass)
        loadButton.classList.remove(hiddenClass)
      }
      def stateStop(){
        loadButton.classList.add(hiddenClass)
        stopButton.classList.remove(hiddenClass)  
      }
      def disable(){
        root.disabled=true
        loadButton.classList.add("disabled")
        stopButton.classList.add("disabled")
      }
      def enable(){
        root.disabled=false
        loadButton.classList.remove("disabled")
        stopButton.classList.remove("disabled")
      }

      def simulatorEvent(){
        updateUI()
      }
      def simulatorEvent(i:InstructionInfo){
         simulatorEvent() 
      }
      
      def compilationEvent(){
        updateUI()
      }
      
      
      
      
      
    }
  
    def buttonFactory(s:String,t:String,iconClass:String)={
         a(cls:="controlButton btn btn-primary"
//        ,img(cls:="",src := imageUrl, alt := s)
        ,i(cls:=s"fa $iconClass")     
        ,s
        ,title := t
        ).render
    }

  val quickButton = buttonFactory("Quick Run","F1: Reset simulator, load program into memory, run until cpu stops.","fa-play-circle")

  val loadOrStopButton= new LoadOrStopButton(s)
  
  val runOneButton   = buttonFactory("Step", "F6: Execute a single instruction.", "fa-step-forward")
  val runButton  = buttonFactory("Finish", "F5: Run program until cpu stops..", "fa-play")
  
          
 


  val root = span(id := "controls"
      ,span(cls := "controlSectionStart")
      ,span(cls := "controlSection", quickButton)
      ,span(cls := "controlSectionSeparator")
      ,span(cls := "controlSection", loadOrStopButton.root)
      ,span(cls := "controlSection",runButton)
     ,span(cls := "controlSection",runOneButton)
     
      ).render

  def disableButton(bootstrapButton:Anchor){
    bootstrapButton.classList.add("disabled")
  }
  def enableButton(bootstrapButton:Anchor){
    bootstrapButton.classList.remove("disabled")
  }
  def setEnabled(bootstrapButton:Anchor,state:Boolean){
    if (state){
      enableButton(bootstrapButton)
    }else{
      disableButton(bootstrapButton)
    }
      
  }
  def disable(){
    setEnabled(quickButton,false)
    setEnabled(runButton,false)
    setEnabled(runOneButton,false)
    loadOrStopButton.disable()
  }
  def updateUI(){  
    
    setEnabled(quickButton,s.canLoadOrQuickRun())
    setEnabled(runButton,s.isSimulatorExecuting())
    setEnabled(runOneButton,s.isSimulatorExecuting())
    
    
    if (s.canLoadOrQuickRun()){
      loadOrStopButton.stateLoad()
      loadOrStopButton.enable()
    }else if (s.isSimulatorExecuting() ){
      loadOrStopButton.stateStop()
      loadOrStopButton.enable()
    }else{
      loadOrStopButton.stateLoad()
      loadOrStopButton.disable()
    }
    
    
  }
  
  
  def simulatorEvent() {
    updateUI()
    
  }
  def simulatorEvent(i:InstructionInfo) {
    simulatorEvent()
  }
  def compilationEvent(){
    updateUI()   
  }
}
