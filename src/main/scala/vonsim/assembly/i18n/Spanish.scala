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
  def emptyProgram="El programa no contiene instrucciones. Debe tener, como mínimo, una sentencia END."
  def missingEnd="Falta una sentencia END para terminar el programa."
}