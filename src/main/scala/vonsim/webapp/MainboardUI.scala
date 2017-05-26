package vonsim.webapp
import vonsim.simulator.InstructionInfo
import vonsim.utils.CollectionUtils._
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import org.scalajs.dom
import scala.scalajs.js
import js.JSConverters._
import scala.collection.mutable
import vonsim.simulator.Simulator
import scala.util.Random
import vonsim.simulator
import vonsim.simulator.Flags
import vonsim.simulator.DWord
import vonsim.simulator.Word
import vonsim.simulator.FullRegister
import scalatags.JsDom.all._
import vonsim.simulator.Flag
import vonsim.simulator.SimulatorProgramLoaded
import vonsim.simulator.SimulatorProgramExecuting
import vonsim.simulator.SimulatorNoProgramLoaded
import vonsim.simulator.SimulatorExecutionError
import vonsim.simulator.SimulatorExecutionFinished


class ControlsUI(s: Simulator) extends VonSimUI(s) {
  
  
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
  

  val root = div(id := "mainboardControls" 
     ,span(cls := "controlSection",resetButton)
     ,span(cls := "controlSection",runOneButton)
     ,span(cls := "controlSection",runPauseButton)
      ).render
  
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

class MainboardUI(s: Simulator) extends VonSimUI(s) {
  val cpuUI = new CpuUI(s)
  val memoryUI = new MemoryUI(s)
//  val ioMemoryUI = new IOMemoryUI(s)
//  val devicesUI = new DevicesUI(s)
  val controlsUI = new ControlsUI(s)


  val console = pre("").render
  val consoleDir = div(id := "console",
    h2("Console"),
    console).render

  val root = div(id := "mainboard", controlsUI.root, div(id := "devices"
    ,cpuUI.root
    ,memoryUI.root
//    ,ioMemoryUI.root
//    ,devicesUI.root
    )).render
    
   def update() {
    memoryUI.update()
    cpuUI.update()
    controlsUI.update()
//    ioMemoryUI.update()
  }
  def update(i:InstructionInfo) {
    memoryUI.update(i)
    cpuUI.update(i)
    controlsUI.update(i)
//    ioMemoryUI.update(i)
  }
  
}

class IOMemoryUI(s: Simulator) extends VonSimUI(s) {
  val memoryTable = table(
    thead(th("Name"), th("Address"), th("Value"))).render
  val r = new Random()
  val names = Map(10 -> "PA", 11 -> "PB", 12 -> "CA", 13 -> "CB")
  for (i <- 0 to 128) {
    val address = formatIOAddress(i)
    val value = formatWord(Word(r.nextInt(256)))
    val name = names.getOrElse(i, "")
    memoryTable.appendChild(tr(td(name), td(address), td(value)).render)
  }

  val root = div(id := "iomemory", cls := "memory",
    div(cls := "flexcolumns",
      img(id := "iomemoryicon", src := "img/motherboard/cable.png"), h2("IO Memory")),
    div(cls := "memoryTable", memoryTable)).render
    
  def update() {
   //TODO devices  
  }
  def update(i:InstructionInfo) {
   //TODO devices 
  }

}

class MemoryUI(s: Simulator) extends VonSimUI(s) {

  val body = tbody(id := "memoryTableBody", cls := "clusterize-content").render

  val memoryTable = table(
    thead(th("Address"), th("Value")), body).render
  val memoryTableDiv = div(id := "memoryTable", cls := "memoryTable clusterize-scroll", memoryTable).render
  val root = div(id := "memory", cls := "memory",
    div(cls := "flexcolumns",
      img(id:= "memoryicon", src := "img/motherboard/ram.png"), h3("Memory")),
    memoryTableDiv).render
    
  var stringRows=generateRows().toJSArray
  val clusterizePropsElements = new ClusterizeProps {
    override val rows=Some(stringRows).orUndefined
    override val scrollElem = Some(memoryTableDiv).orUndefined
    override val contentElem = Some(body).orUndefined
  }
  
  val clusterize = new Clusterize(clusterizePropsElements)
  
  def generateRows()={
    (0 until s.memory.values.length).map(generateRow).toArray
  }
  def generateRow(i:Int)={
    val address = formatAddress(i) 
    val value = formatWord(s.memory.values(i))
      s"<tr> <td> $address </td> <td> $value </td> </tr>"
  }
  
  def addressToId(address:String)={
    s"memory_address_$address"
  }
  def update() {
    stringRows=generateRows().toJSArray
    clusterize.update(stringRows)
  }
  def update(i:InstructionInfo) {
    // TODO
    update()
  }

}


class RegistersUI(s: Simulator,val registers:List[FullRegister],title:String,baseId:String="") extends VonSimUI(s){
  
  def getIdFor(part:String)=if (baseId=="") "" else baseId+part

  val body = tbody(id:= getIdFor("TableBody"), cls := "registersTableBody").render
  var registerToValueL=mutable.Map[FullRegister,TableCell]()
  var registerToValueH=mutable.Map[FullRegister,TableCell]()
  
  registers.foreach(r => {
    val valueElementH=td("00").render
    val valueElementL=td("00").render
    registerToValueL(r)=valueElementL
    registerToValueH(r)=valueElementH
    body.appendChild( tr(td(r.toString()),valueElementH,valueElementL).render )
  })
  
  
  val registerTable = table(cls := "registerTable",
    thead(th("Register"), th(colspan := 2, "Value")),
    thead(th(""), th("H"), th("L")),
    body
    ).render
    
  val root = div(id := getIdFor("RegistersTable"), cls:="cpuElement",
    div(cls := "flexcolumns",
      img(cls := "registersIcon", src := "img/motherboard/register_icon.png"), h3(title)),
      registerTable
   ).render
   
  def update(){
    registers.foreach(r=>{
      val value=s.cpu.get(r)
      registerToValueL(r).textContent=formatWord(value.l)
      registerToValueH(r).textContent=formatWord(value.h)
    })
  }
  def update(i:InstructionInfo){
    update()
  }
}

class WordUI extends HTMLUI {
  val wordElement=td("00000000 00000000").render
  val root=table(cls := "bitTable", tr(wordElement)).render
  
  def update(v:DWord){
    val l=Word(v.l).bitString.reverse
    val h=Word(v.h).bitString.reverse
    wordElement.textContent=h+" "+l
  }
}

class FlagsUI extends HTMLUI {
  
  val flagElements=Flag.all.map(f=> (f,span("0").render)).toMap
  
  val root=span(cls := "flagsTable"
  ).render
  flagElements.foreach(f => root.appendChild(span(cls:="flag",f._1.toString+" = ",f._2).render))
  
  def flagAsString(flag:Boolean)= if (flag) "1" else "0"
  def update(flags:Flags){
    flagElements.foreach(f =>{
      f._2.textContent=flagAsString(flags.get(f._1))
    })
    
  }
}

class AluUI(s: Simulator) extends VonSimUI(s) {
  
  val bitTableA = new WordUI()
  val bitTableB = new WordUI()
  val resultBitTable = new WordUI()
  val flagsUI=new FlagsUI()
  val operation = span(cls:="operation","--").render
  val root = div(id := "alu", cls := "cpuElement",
    h3("ALU")
    ,div("Operand A:", bitTableA.root)
    ,div("Operation:", operation)
    ,div("Operand B:", bitTableB.root)
    ,hr()
    ,div("Result:", resultBitTable.root)
    ,div(span("Flags:"), flagsUI.root)
    ).render
    
  def update(){
    operation.textContent=s.cpu.alu.op.toString()
    bitTableA.update(s.cpu.alu.o1)
    bitTableB.update(s.cpu.alu.o2)
    resultBitTable.update(s.cpu.alu.res)
    flagsUI.update(s.cpu.alu.flags)
  }  
  def update(i:InstructionInfo){
    
  }
}

class CpuUI(s: Simulator) extends VonSimUI(s) {
  val generalPurposeRegistersTable = new RegistersUI(s,List(simulator.AX,simulator.BX,simulator.CX,simulator.DX),"General Purpose Registers","generalPurpose")
  val specialRegistersTable = new RegistersUI(s,List(simulator.IP,simulator.SP),"Special Registers","special")
  val alu=new AluUI(s)
  

  val root = div(id := "cpu",
    div(cls := "flexcolumns",
      img(id := "cpuicon", src := "img/motherboard/microchip.png"), h2("CPU")),
    generalPurposeRegistersTable.root,
    specialRegistersTable.root,
    alu.root).render

  def update() {
    generalPurposeRegistersTable.update()
    specialRegistersTable.update()
    alu.update()
  }
  def update(i:InstructionInfo) {
    // TODO improve
    update()
  }
}

class DevicesUI(s: Simulator) extends VonSimUI(s) {

  val root = div(id := "iomemory", cls := "memory",
    div(cls := "flexcolumns",
      img(id := "devicesicon", src := "img/motherboard/printer.png"), h2("Devices")),
    "some pretty devices plz here").render

    def update() {
    //TODO
  }
  def update(i:InstructionInfo) {
    // TODO improve
    update()
  }
}