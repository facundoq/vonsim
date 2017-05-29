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
    case SimulatorNoProgramLoaded => "circle"
    case SimulatorProgramLoaded => "play-circle"
    case SimulatorProgramExecuting => "pause-circle"      
  }
  def stateToButtonClass(state:SimulatorState)= state match{
    case SimulatorExecutionError(msg) => "btn-danger"
    case SimulatorExecutionFinished => "btn-success"
    case SimulatorNoProgramLoaded => "btn-danger"
    case SimulatorProgramLoaded => "btn-warning"
    case SimulatorProgramExecuting => "btn-warning"      
  }
  def stateToMessage(state:SimulatorState)= state match{
    case SimulatorExecutionError(msg) => "Error"
    case SimulatorExecutionFinished => "Execution Finished"
    case SimulatorNoProgramLoaded => "No Program Loaded"
    case SimulatorProgramLoaded => "Program Loaded"
    case SimulatorProgramExecuting => "Program executing"      
  }
  def stateToTooltip(state:SimulatorState)= state match{
    case SimulatorExecutionError(msg) => msg
    case SimulatorExecutionFinished => "The execution has finished or has been stopped. Reload the program to execute again or perform a Quick Run."
    case SimulatorNoProgramLoaded => "There is no program loaded in the simulator; you must load one before executing, or perform a Quick Run"
    case SimulatorProgramLoaded => "The program has been loaded succesfully into the simulator and is ready to execute."
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
      val loadButton = buttonFactory("Load","F2: Load program into memory without starting execution.","fa-download")
      val stopButton = buttonFactory("Stop", "F3: Leave step mode.", "fa-stop")
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
      def updateUI(){
        s.c match{
          case Left(x) => {
            disable()
          }
          case Right(x) => enable()
           
        }
        if (s.s.state==SimulatorProgramExecuting || s.s.state==SimulatorProgramLoaded){
          stateStop()
        }else{
          stateLoad()
        }
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

  val quickButton = buttonFactory("Quick run","F1: Reset simulator, load program into memory, run until cpu stops.","fa-play-circle")

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

  def disable(bootstrapButton:Anchor){
    bootstrapButton.classList.add("disabled")
  }
  def enable(bootstrapButton:Anchor){
    bootstrapButton.classList.remove("disabled")
  }
  
  def updateUI(){  
    
    
    s.c match{
      case Left(x) => {
        disable(quickButton)
      }
      case Right(x) =>{
        enable(quickButton)
      }
    }
    
    s.s.state  match{
      case SimulatorNoProgramLoaded => {
        disable(runButton)
        disable(runOneButton)
      }
      case SimulatorProgramExecuting => {
        disable(quickButton)
        enable(runButton)
        enable(runOneButton)
      }
      case SimulatorProgramLoaded => {
        enable(runButton)
        enable(runOneButton)
      }
      case ( SimulatorExecutionFinished | SimulatorExecutionError(_)) => {
        disable(runButton)
        disable(runOneButton)
      }
    }
    loadOrStopButton.updateUI()
    
    
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
