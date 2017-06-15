package vonsim.webapp
import vonsim.simulator._
import scalatags.JsDom.all._
import vonsim.assembly.Compiler.CompilationResult

class HelpUI(s:VonSimState) extends VonSimUI(s) {
  
  val modal = div(
      cls:="modal fade"
      ,role:="dialog"
      ,id:="helpModal"
//      ,style:="display:none"
      ,div(cls:="modal-dialog"
        ,div(cls:="modal-content"
            ,div(cls:="modal-header"
              ,button(`type`:="button",cls:="close", data("dismiss"):="modal",i(cls:="fa fa-close"))
              ,h4(cls:="modal-title","VonSim (beta)")
            )
            ,div(cls:="modal-body"
                ,p("A simplified intel 8088 simulator in the spirit of MSX88")
                ,p(a(href:="https://github.com/facundoq/vonsim","Github page"))
                ,p(a(href:="https://github.com/facundoq/vonsim/issues","Issues"))
                ,p("This simulator is intended for use in the Universidad Nacional de La Plata classes:")
                ,p(a(href:="http://weblidi.info.unlp.edu.ar/catedras/organiza/","Organización de Computadoras"))
                ,p(a(href:="http://weblidi.info.unlp.edu.ar/catedras/arquitecturaP2003/","Arquitectura de Computadoras"))
            )
            ,div(cls:="modal-footer"
                ,p("Made by ", a(href:="https://github.com/facundoq/","Facundo Quiroga"))
                ,p("With help from "
                    ,a(href:="https://github.com/AndoniZubimendi","Andoni Zubimendi")
                    ," and "
                    ,a(href:="https://github.com/cesarares","Cesar Estrebou")
                    )
                ,p("Feedback is welcome at f<last name> (at) gmail.com")
            )
          )
        )
      )
  val root=span(id := "help"
      ,a(cls:="controlButton btn btn-default"
      ,data("toggle"):="modal"
      ,data("target"):="#helpModal"
      ,i(cls:=s"fa fa-question")
      )
      ,modal
      ).render
  
  def simulatorEvent() {
  }
  
  def simulatorEvent(i:InstructionInfo) {
  }
  def compilationEvent(){
  }
}


class HeaderUI(s: VonSimState) extends VonSimUI(s) {
  val controlsUI = new ControlsUI(s)
  val simulatorStateUI= new SimulatorStateUI(s)
  val helpUI= new HelpUI(s)
  
  val root=header(div(id := "header"
      , img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
      , controlsUI.root
      //,span(cls:="controlSectionSeparator")
      , simulatorStateUI.root
      , helpUI.root
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
