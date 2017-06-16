package vonsim.webapp
import vonsim.simulator._
import scalatags.JsDom.all._
import vonsim.assembly.Compiler.CompilationResult
import org.scalajs.dom.raw.HTMLElement

abstract class ModalUI(s:VonSimState) extends VonSimUI(s){
  val root = div(
      cls:="modal fade"
      ,role:="dialog"
      ,id:="helpModal"
//      ,style:="display:none"
      ,div(cls:="modal-dialog"
        ,div(cls:="modal-content"
            ,div(cls:="modal-header"
                ,getHeader()
            )
            ,div(cls:="modal-body"
                ,getBody()
            )
            ,div(cls:="modal-footer"
                ,getFooter()
            )
          )
        )
      ).render
  def getHeader():HTMLElement
  def getBody():HTMLElement
  def getFooter():HTMLElement
}

class HelpUI(s:VonSimState) extends ModalUI(s){
  
  def getHeader()={
    div(cls:="modal-header-help",img(cls:= "modal-icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
              ,h4(cls:="modal-title","A simplified 8088 simulator")
              ,button(`type`:="button",cls:="close", data("dismiss"):="modal",i(cls:="fa fa-close"))
    ).render
  }
  
  def getBody()={
    div(cls:=""
      ,p("A simplified intel 8088 simulator in the spirit of MSX88")
      ,p(a(cls:="btn btn-success",href:="https://github.com/facundoq/vonsim","Github page")
      ,a(cls:="btn btn-success",href:="https://github.com/facundoq/vonsim/issues","Report issue"))
      ,p("This simulator is intended for use in the Universidad Nacional de La Plata classes:")
      ,p(a(cls:="btn btn-success",href:="http://weblidi.info.unlp.edu.ar/catedras/organiza/","Organización de Computadoras")
      ,a(cls:="btn btn-success",href:="http://weblidi.info.unlp.edu.ar/catedras/arquitecturaP2003/","Arquitectura de Computadoras"))
    ).render
  }
  
  def getFooter()={
    div(cls:=""
     ,p("Made by ", a(cls:="btn btn-primary",href:="https://github.com/facundoq/","Facundo Quiroga")
    		 
          ," with help from "
    ,a(cls:="btn btn-primary",href:="https://github.com/AndoniZubimendi","Andoni Zubimendi")
    ," and "
    ,a(cls:="btn btn-primary",href:="https://github.com/cesarares","Cesar Estrebou")
                    )
,p("Feedback is welcome at f<last name> (at) gmail.com")
    ).render
  }
  
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
  
  val helpUIButton=span(id := "help"
      ,a(cls:="helpButton"
      ,data("toggle"):="modal"
      ,data("target"):="#helpModal"
      ,i(cls:=s"fa fa-question-circle")
      )
      ,helpUI.root
      ).render
  
  val root=header(div(id := "header"
      , img(id := "icon", alt := "Von Sim Icon", title := "Von Sim: a simplified intel 8088 simulator", src := "img/icon.png")
      , controlsUI.root
      //,span(cls:="controlSectionSeparator")
      
      , helpUIButton
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
