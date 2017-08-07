package vonsim.webapp
import vonsim.utils.CollectionUtils._
import vonsim.simulator.InstructionInfo
import scala.scalajs.js.annotation.ScalaJSDefined
import scala.scalajs.js.annotation.JSName
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scala.scalajs.js
import js.JSConverters._
import vonsim.simulator.Simulator
import scalatags.JsDom.all._



import vonsim.simulator.SimulatorProgramExecuting
import vonsim.assembly.Compiler.CompilationResult

import vonsim.assembly.Location
import vonsim.assembly.LexerError
import vonsim.assembly.ParserError
import vonsim.assembly.SemanticError
import vonsim.assembly.CompilationError
import scala.collection.mutable.ListBuffer
import vonsim.webapp.tutorials.Tutorial

class TutorialUIControl(s: VonSimState,val tutorial:Tutorial,tutorialUpdated:Function0[Unit]) extends VonSimUI(s) {
  
  def buttonFactory(text:String,iconClass:String)=a(cls:="tutorialButton btn btn-primary"
//        ,img(cls:="",src := imageUrl, alt := s)
        ,i(cls:=s"fa $iconClass")     
        ,text
        ,title := text
        ).render
  val nextButton=buttonFactory("Siguiente","fa-next")
  val previousButton=buttonFactory("Anterior","fa-previous")
  val current=span().render
  val total=span().render
  val root=div(id:="tutorialControls"
      ,previousButton
      ,span(id:="tutorialCount",current,"/",total)
      ,nextButton
      ).render
  update() 
  def setDisabled(button:Anchor,disabled:Boolean){
    disabled match{
      case true => button.classList.add("disabled")
      case false => button.classList.remove("disabled")
    }
  }
  
  def update(){
    setDisabled(nextButton, !tutorial.canForward(s))
    setDisabled(previousButton, !tutorial.canBackward(s))
    current.textContent=(tutorial.step+1).toString()
    total.textContent=tutorial.steps.length.toString()
    
  }
  previousButton.onclick=(e:Any) =>{
    tutorial.previous
    update()
    tutorialUpdated.apply()
  }
  nextButton.onclick=(e:Any) =>{
    tutorial.next
    update()
    tutorialUpdated.apply()
  }
  
  def simulatorEvent() {
  }
  
  def simulatorEvent(i:InstructionInfo) {
    simulatorEvent()
  }
  
  def compilationEvent(){
    
  }
  
}

class TutorialUI(s: VonSimState,val tutorial:Tutorial,val mainUI:MainUI) extends VonSimUI(s) {

  
  val controls=new TutorialUIControl(s,tutorial,() => {
    displayTutorialStep()
  })
  val content=p().render
  val subtitle=span(id:="tutorialStepTitle").render
  val title=span(id:="tutorialTitle").render
  val header=h3(title,subtitle)
  val root = div(id := "tutorial"
     ,header
     ,div(id:="tutorialContent",content)
     ,controls.root
     ).render
  
  
  def startTutorial(){
    displayTutorialStep()  
  }
  def displayTutorialStep(){
    title.textContent=tutorial.title
    subtitle.textContent=tutorial.current.title
    content.innerHTML=tutorial.current.content
    mainUI.applyUIConfig(tutorial.current.config)
  }
  def simulatorEvent() {
    // TODO check if code can be run and if the cpu is halted to allow enable buttons    
    if (s.isSimulatorExecuting()){
      disable()
    }else{
      enable()
    }
  }
  
//  def disable(){root.disabled=true}
//  def enable() {root.disabled=false}
  
  def simulatorEvent(i:InstructionInfo) {
    simulatorEvent()
  }
  
  
  def compilationEvent(){
    s.c match {
      case Left(f) => {
        
      }
      case Right(f) => {
      }
    }
  }
}



