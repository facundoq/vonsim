package vonsim.assembly.i18n

object English{
  def code="en"
}
class English extends CompilerLanguage{
  def code=English.code
  def newline="newline"
  def stringLiteral="string literal"
  def identifier="identifier"
  
  def label="label"
  def integerLiteral="number literal"
  def offsetLabel="offset <label>"
  def equLabel="equ's label"
  
}