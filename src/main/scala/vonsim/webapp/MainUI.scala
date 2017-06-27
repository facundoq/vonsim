
package vonsim.webapp
import vonsim.utils.CollectionUtils._
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scala.scalajs.js
import js.JSConverters._
import scala.scalajs.js.timers._
import scala.collection.mutable

import scala.util.parsing.input.OffsetPosition
import vonsim.assembly.lexer.VonemuPosition

import vonsim.assembly.Compiler
import org.scalajs.dom.raw.HTMLElement

import vonsim.assembly.Compiler.FailedCompilation

import vonsim.simulator.Simulator
import vonsim.simulator.InstructionInfo
import vonsim.simulator.DWord
import vonsim.simulator.Word
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.SuccessfulCompilation

import vonsim.simulator.SimulatorProgramExecuting
import vonsim.assembly.Compiler.CompilationResult
import vonsim.assembly.Compiler.SuccessfulCompilation
import vonsim.assembly.Compiler.SuccessfulCompilation
import com.scalawarrior.scalajs.ace.IEditSession
import vonsim.assembly.CompilationError
import vonsim.simulator.InstructionInfo
import vonsim.assembly.Compiler.CompilationResult



class MainUI(s: VonSimState, defaultCode: String,saveCodeKey:String) extends VonSimUI(s) {
  
  println("Setting up UI..")
  val editorUI = new EditorUI(s, defaultCode, () => {
   saveCode()
   compile()   
  })
 
  
  
  val mainboardUI = new MainboardUI(s)
  val headerUI=new HeaderUI(s)
  val sim = div(id := "main",
      editorUI.root,
    mainboardUI.root).render

  val root = div(id := "pagewrap"
      ,headerUI.root 
      ,sim).render

  
  bindkey(root,s.uil.controlsQuickHotkey,() => {
    if (s.canLoadOrQuickRun()){
      quickRun()
    }
    false
    })
  
  bindkey(root,s.uil.controlsDebugOrAbortHotkey,() => {
    if (s.canLoadOrQuickRun()){
      loadProgram()
    }else if (s.isSimulatorExecuting()){
      stop()
    }
    false
    })
    
  bindkey(root,s.uil.controlsFinishHotkey,() => {
    if (s.isSimulatorExecuting()){
      runInstructions()
    }
    false
    })
    
  bindkey(root,s.uil.controlsStepHotkey,() => {
    if (s.isSimulatorExecuting()){
      stepInstruction()
    }
    false
    })
  
  bindkey(root,"ctrl+s",() => {
    false
  })
  
  
  headerUI.controlsUI.quickButton.onclick=(e:Any) =>{quickRun()}
  
  headerUI.controlsUI.loadOrStopButton.loadButton.onclick=(e:Any) =>{
    loadProgram()
  }
  headerUI.controlsUI.loadOrStopButton.stopButton.onclick=(e:Any) =>{
      stop()
  }
 
  headerUI.controlsUI.finishButton.onclick=(e:Any) =>{runInstructions()}
  headerUI.controlsUI.stepButton.onclick=(e:Any) =>{stepInstruction()}
     

  println("UI set up. Updating for the first time..")
  simulatorEvent()
  compilationEvent()
  
  
  
  def compilationEvent() {
    println("compilationEvent triggered "+ s.c)
    headerUI.compilationEvent()
    editorUI.compilationEvent()
  }  

  def simulatorEvent(){
    println("simulatorEvent triggered")
    headerUI.simulatorEvent()
    editorUI.simulatorEvent()
    mainboardUI.simulatorEvent()
  }
  def simulatorEvent(i:InstructionInfo) {
    println(s"simulatorEvent instruction $i triggered")
    headerUI.simulatorEvent(i)
    editorUI.simulatorEvent(i)
    mainboardUI.simulatorEvent(i)
  }
  
  def compile(){
    val codeString = editorUI.getCode()
    s.c= Compiler(codeString)
    compilationEvent()
  }
  
  def reset(){
    println("Resetting	... ")
   s.s.reset()
   simulatorEvent()
  }
  def stop(){
   println("Stopping execution... ")
   s.s.stop()
   simulatorEvent()
  }
  def runInstructions(){
     println("Running... ")
     editorUI.disable()
     headerUI.controlsUI.disable()
     setTimeout(50)({ 
       val instructions=s.s.runInstructions()
       simulatorEvent()
       if (instructions.length>0 && instructions.last.isLeft){
         val error = instructions.last.left.get
        // executionError(error.message)
       }
      })

  }
  

  def stepInstruction(){
     println("Step instruction.. ")
     val i=s.s.stepInstruction()
     i match{
       case Left(error) => //executionError(error.message)
       case Right(i) => simulatorEvent(i)
     }
     
    
  }
  def quickRun(){
    s.c match {
       case Right(c:SuccessfulCompilation) => {
          loadProgram()
          runInstructions() 
          
          
       }
       case _ => dom.window.alert(s.uil.alertCompilationFailed)
     }
   
    
  }
  def loadProgram(){
    s.c match {
       case Right(c:SuccessfulCompilation) => {
         println("Loading program... ")
        s.s.load(c)
        simulatorEvent()
        println("Done")
       }
       case _ => dom.window.alert(s.uil.alertCompilationFailed)
     }
    
  }
  def saveCode(){
    dom.window.localStorage.setItem(saveCodeKey, editorUI.getCode())
  }

}
