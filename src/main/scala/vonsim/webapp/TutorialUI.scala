package vonsim.webapp
import vonsim.utils.CollectionUtils._
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



import vonsim.simulator.SimulatorProgramExecuting
import vonsim.assembly.Compiler.CompilationResult

import vonsim.assembly.Location
import vonsim.assembly.LexerError
import vonsim.assembly.ParserError
import vonsim.assembly.SemanticError
import vonsim.assembly.CompilationError
import scala.collection.mutable.ListBuffer
import vonsim.webapp.tutorials.Tutorial

class TutorialUIControl(s: VonSimState,val tutorial:Tutorial) extends VonSimUI(s) {
  
  def buttonFactory(text:String,iconClass:String)=a(cls:="tutorialButton btn btn-primary"
//        ,img(cls:="",src := imageUrl, alt := s)
        ,i(cls:=s"fa $iconClass")     
        ,text
        ,title := text
        ).render
  val nextButton=buttonFactory("Siguiente","fa-next")
  val previousButton=buttonFactory("Anterior","fa-previous")
  val root=div(id:="tutorialControls"
      ,previousButton
      ,nextButton
      ).render
      
  previousButton.onclick=(e:Any) =>{
    
  }
  
  def simulatorEvent() {
  }
  
  def simulatorEvent(i:InstructionInfo) {
    simulatorEvent()
  }
  
  def compilationEvent(){
    
  }
  
}

class TutorialUI(s: VonSimState,val tutorial:Tutorial) extends VonSimUI(s) {

  
  val controls=new TutorialUIControl(s,tutorial)
  val content=p().render
  val title=h3().render
  val root = div(id := "tutorial"
     ,title
     ,div(id:="tutorialContent",content)
     ,controls.root
     ).render
  
  title.textContent=tutorial.current.title
  content.innerHTML=tutorial.current.content
  
  def simulatorEvent() {
    // TODO check if code can be run and if the cpu is halted to allow enable buttons    
    if (s.isSimulatorExecuting()){
      disable()
    }else{
      enable()
    }
  }
  
  def disable(){root.disabled=true}
  def enable() {root.disabled=false}
  
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



