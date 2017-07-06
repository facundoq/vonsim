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
import java.awt.Window

import vonsim.assembly
import vonsim.webapp.i18n.Spanish
import vonsim.webapp.i18n.UILanguage
import vonsim.assembly.i18n.CompilerLanguage
import vonsim.simulator.i18n.SimulatorLanguage


class Languages(val uiLanguage:UILanguage,val compilerLanguage:CompilerLanguage,val simulatorLanguage:SimulatorLanguage){
  
}

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
  val codeURLKey="url"
  val cookieLanguageKey="lang"
  def saveCodeKey="code"
  var fallbackLanguage=Spanish.code

  
  def main(): Unit = {
    val languageCode=getLanguageCode()
    val languages=getLanguages(languageCode)    
    val parameters = getParameters()

    if (parameters.keySet.contains(codeURLKey)){
      val url=parameters(codeURLKey)
      val headers=Map("crossDomain" -> "true"
      ,"Access-Control-Allow-Origin" -> "*"
      , "dataType"-> "text")
      
      val promise=Ajax.get(url,timeout=5000,headers=headers,responseType="text")
      promise.onSuccess{ 
        case xhr =>
          initializeUI(xhr.responseText,languages)
      }
      promise.onFailure{
        case xhr =>
          initializeUI(defaultCode,languages)
          dom.window.alert(languages.uiLanguage.alertURLNotFound(url))
      }
      
    }else{
      val lastCode=dom.window.localStorage.getItem(saveCodeKey)
      if (lastCode!=null){
        initializeUI(lastCode,languages)
      }else{
        initializeUI(defaultCode,languages)
      }
    }
    
    
  }
  var ui:MainUI=null
  var s:VonSimState=null
  
  def initializeUI(initialCode:String,l:Languages){
    
    val compilationResult=Compiler(initialCode)

    Compiler.language=l.compilerLanguage
    val simulator=Simulator.Empty()
    simulator.language=l.simulatorLanguage
    
    var s=new VonSimState(simulator,compilationResult,l.uiLanguage)
    
    ui = new MainUI(s,initialCode,saveCodeKey)
    document.body.appendChild(ui.root)
    ui.editorUI.editor.resize(true)
    setTimeout(2000)({ui.editorUI.editor.resize(true)})
  }
  def getLanguageCode():String={
      println("Detected language: "+dom.window.navigator.language)
      var language=dom.window.localStorage.getItem(cookieLanguageKey)
      if (language==null){
         if (dom.window.navigator.language != null && dom.window.navigator.language.trim() != ""){
             language=dom.window.navigator.language.split("-")(0)
             language=language.toLowerCase
          }else{
            language=fallbackLanguage
          }
      }
      dom.window.localStorage.setItem(cookieLanguageKey,language)
      language
  }
  def languageCodeToObject[T](languageToObject:Map[String,T],language:String)={
    if (languageToObject.keySet.contains(language)){
      languageToObject(language)
    }else{
      languageToObject(fallbackLanguage)
    }
   
  }
  def getLanguages(language:String)={
    val uiLanguage=languageCodeToObject(UILanguage.codes, language)
    val compilerLanguage=languageCodeToObject(CompilerLanguage.codes, language)
    val simulatorLanguage=languageCodeToObject(SimulatorLanguage.codes, language)
    new Languages(uiLanguage,compilerLanguage,simulatorLanguage)
  }
  

  def defaultCode = """org 1000h
; variables here


org 2000h
; your code here
hlt
end
"""

}





  