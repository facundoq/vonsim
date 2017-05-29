package vonsim.webapp
import org.scalajs.dom.raw.HTMLElement

import vonsim.simulator.Simulator
import vonsim.simulator.InstructionInfo
import vonsim.simulator.DWord
import vonsim.simulator.Word
import vonsim.assembly.Compiler.CompilationResult

abstract class HTMLUI {
  def root: HTMLElement
}

class VonSimState(var s:Simulator, var c:CompilationResult){
  
}

abstract class VonSimUI(val s: VonSimState) extends HTMLUI{

  def simulatorEvent() // update changes made to the simulator
  def simulatorEvent(i:InstructionInfo) // update UI after execution of instruction
  def compilationEvent()

  
  def formatIOAddress(a:Int)={
    "%02X".format(a)
  }
  def formatAddress(a:Int)={
    "%04X".format(a)
  }
  def formatWord(a:Word)={
    "%02X".format(a.toUnsignedInt)
  }
  def formatDWord(a:DWord)={
    "%04X".format(a.toUnsignedInt)
  }
}






