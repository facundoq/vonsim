package vonsim.webapp

// tutorial https://www.scala-js.org/tutorial/basic/
// canvas https://github.com/vmunier/scalajs-simple-canvas-game/blob/master/src/main/scala/simplegame/SimpleCanvasGame.scala

import scala.scalajs.js.JSApp
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.Element

import dom.document
import scala.scalajs.js.annotation.JSExport
import scalatags.JsDom.all._
import org.scalajs.dom.html._
import org.scalajs.dom.raw.HTMLElement
import js.JSConverters._

import java.awt.Event
import scala.util.parsing.input.Position
import scala.scalajs.js.timers._

import dom.ext._
import scala.scalajs.concurrent
            .JSExecutionContext
            .Implicits
            .queue
import vonsim.simulator.Simulator
import vonsim.assembly.Compiler

            

object Main extends JSApp {

  def getParameters()={
    val parametersTuple=dom.window.location.search
                   .substring(1)
                   .split("&")
                   .map(js.URIUtils.decodeURIComponent)
                   .map(q => q.split("="))
                   .map { q => (q(0),q(1)) }
    parametersTuple.toMap
  }
  def main(): Unit = {
    val codeURLKey="url"
    val parameters = getParameters()

    if (parameters.keySet.contains(codeURLKey)){
      val url=parameters(codeURLKey)
      val headers=Map("crossDomain" -> "true"
      ,"Access-Control-Allow-Origin" -> "*"
      , "dataType"-> "text")
      
      val promise=Ajax.get(url,timeout=5000,headers=headers,responseType="text")
      promise.onSuccess{ 
        case xhr =>
          initializeUI(xhr.responseText)
      }
      promise.onFailure{
        case xhr =>
          initializeUI(defaultCode)
          dom.window.alert("Could not load URL "+url+"\n")
      }
      
    }else{
      initializeUI(defaultCode)
    }
    
    
  }
  var ui:MainUI=null
  var s:VonSimState=null
  
  def initializeUI(initialCode:String){
    val simulator=Simulator.Empty()
    val compilationResult=Compiler(initialCode)
    var s=new VonSimState(simulator,compilationResult)
    
    ui = new MainUI(s,initialCode)
    document.body.appendChild(ui.root)
    ui.editorUI.editor.resize(true)
    setTimeout(2000)({ui.editorUI.editor.resize(true)
      })
  } 

  def defaultCode = """org 1000h
; variables here


org 2000h
; your code here
hlt
end
"""

}





  