package vonsim.assembly.i18n

object Spanish{
  def code="es"
}
class Spanish extends CompilerLanguage{
  def code=Spanish.code
  def newline="final de línea"
  def stringLiteral="""cadena de caracteres literal ("") """
  def identifier="identificador"
  def label="etiqueta"
  def integerLiteral="número literal"
  def offsetLabel="offset <etiqueta>"
  def equLabel="etiqueta del equ"
  
}