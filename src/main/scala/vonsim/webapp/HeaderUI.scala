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
              ,img(cls:= "modal-icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
              ,h4(cls:="modal-title","A simplified 8088 simulator")
              ,button(`type`:="button",cls:="close", data("dismiss"):="modal",i(cls:="fa fa-close"))
            )
            ,div(cls:="modal-body"
                ,p("A simplified intel 8088 simulator in the spirit of MSX88")
                ,p(a(cls:="btn btn-success",href:="https://github.com/facundoq/vonsim","Github page")
                ,a(cls:="btn btn-success",href:="https://github.com/facundoq/vonsim/issues","Report issue"))
                ,p("This simulator is intended for use in the Universidad Nacional de La Plata classes:")
                ,p(a(cls:="btn btn-success",href:="http://weblidi.info.unlp.edu.ar/catedras/organiza/","Organización de Computadoras")
                ,a(cls:="btn btn-success",href:="http://weblidi.info.unlp.edu.ar/catedras/arquitecturaP2003/","Arquitectura de Computadoras"))
            )
            ,div(cls:="modal-footer"
                ,p("Made by ", a(cls:="btn btn-primary",href:="https://github.com/facundoq/","Facundo Quiroga")
                ," with help from "
                    ,a(cls:="btn btn-primary",href:="https://github.com/AndoniZubimendi","Andoni Zubimendi")
                    ," and "
                    ,a(cls:="btn btn-primary",href:="https://github.com/cesarares","Cesar Estrebou")
                    )
                ,p("Feedback is welcome at f<last name> (at) gmail.com")
            )
          )
        )
      )
  val root=span(id := "help"
      ,a(cls:="helpButton"
      ,data("toggle"):="modal"
      ,data("target"):="#helpModal"
      ,i(cls:=s"fa fa-question-circle")
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
  
  val helpUI= new HelpUI(s)
  
  val root=header(div(id := "header"
      , img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
      , controlsUI.root
      //,span(cls:="controlSectionSeparator")
      
      , helpUI.root
      )).render
  
  def simulatorEvent() {
    controlsUI.simulatorEvent()
    
  }
  
  def simulatorEvent(i:InstructionInfo) {
        controlsUI.simulatorEvent(i)
        
  }
  def compilationEvent(){
    controlsUI.compilationEvent()
    
  }
  
      
}
