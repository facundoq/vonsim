package vonsim.assembly.i18n

object CompilerLanguage{
  val codes=Map(Spanish.code -> new Spanish()
               ,English.code -> new English()
               )
}
abstract class CompilerLanguage {
  def code:String
  def newline:String
  def stringLiteral:String
  def identifier:String
  def integerLiteral:String
  def label:String
  def offsetLabel:String
  def equLabel:String
  def emptyProgram:String
  def missingEnd:String
}
