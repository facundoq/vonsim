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
    div(cls:="modal-header-help",img(cls:= "modal-icon", alt := "Von Sim Icon", title := s.uil.iconTitle, src := "img/icon.png")
              ,h4(cls:="modal-title",s.uil.pageTitle)
              ,button(`type`:="button",cls:="close", data("dismiss"):="modal",i(cls:="fa fa-close"))
    ).render
  }
  
  def getBody()={
    div(cls:=""
      ,p(s.uil.pageTitleExtended)
      ,p(a(cls:="btn btn-success",href:="https://github.com/facundoq/vonsim",s.uil.helpGithubPage)
      ,a(cls:="btn btn-success",href:="https://github.com/facundoq/vonsim/issues",s.uil.helpReportIssue))
      ,p(s.uil.helpIntendedFor)
      ,p(a(cls:="btn btn-success",href:="http://weblidi.info.unlp.edu.ar/catedras/organiza/","Organización de Computadoras")
      ,a(cls:="btn btn-success",href:="http://weblidi.info.unlp.edu.ar/catedras/arquitecturaP2003/","Arquitectura de Computadoras"))
    ).render
  }
  
  def getFooter()={
    div(cls:=""
     ,p(s.uil.helpMadeBy+" ", a(cls:="btn btn-primary",href:="https://github.com/facundoq/","Facundo Quiroga")
          ," "+s.uil.helpWithHelpFrom+" "
    ,a(cls:="btn btn-primary",href:="https://github.com/AndoniZubimendi","Andoni Zubimendi")
    ," "+s.uil.and+" "
    ,a(cls:="btn btn-primary",href:="https://github.com/cesarares","Cesar Estrebou")
                    )
    ,p(s.uil.helpFeedbackWelcome+" f<last name> (at) gmail.com")
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
      , img(id := "icon", alt := "Von Sim Icon", title := s.uil.iconTitle, src := "img/icon.png")
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
