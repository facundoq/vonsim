package vonsim.webapp.tutorials

import vonsim.webapp.UIConfig

class WhyAssemblyTutorial extends Tutorial {
  val title="Estructura de un programa en Assembler con VonSim"
  
  val initialCode="""
org 2000h
; código aquí
hlt
end
    """
  
  
  val id="whyassembly"
  
  val steps=List(


TutorialStep("Estructura de un programa en Assembler con VonSim"
,"""<p><strong>Objetivo:</strong> Aprender a escribir programas básicos en assembler con el simulador
  VonSim.</p> 

<p><strong>Conocimientos previos:</strong> 
Conocimientos elementales de organización y arquitectura de computadoras y programación.</p>
  
""",UIConfig.disableAll,None
)

,TutorialStep("Introducción",
"""
<p>Si quisieramos programar un procesador sin un lenguaje de programación, 
deberíamos conocer exactamente el código binario de cada 
instrucción del procesador, lo cual resultaría engorroso y propenso a errores.</p>
 
<p>El lenguaje assembly le da nombres a estos códigos, de manera que para sumar 
podamos escribir <code>and</code> en lugar de <code>00000101</code>, por ejemplo.
Por ende, assembly es el lenguaje de programación de más bajo nivel de abstracción que existe, 
ya que es solo una pequeña capa de abstracción sobre los códigos de instrucción del procesador.</p>
""",UIConfig.disableAll)

,TutorialStep("¿Por qué aprender assembly?"
        ,"""<p>El lenguaje assembly es una de las herramientas más antiguas de los programadores.
No obstante, hoy en día casi cualquier tipo de software puede desarrollarse sin siquiera tener 
que mirar una sola línea de código assembly.</p>
<p> ¿Por qué aprenderlo, entonces?  </p>
""",UIConfig.disableAll)

,TutorialStep("La importancia del assembly"
        ,"""
<p>La mejor respuesta es que todo el código que un programador escribe, sea en el lenguaje que sea, en
algún momento se traducirá a código de máquina para ser ejecutado por el procesador, y el lenguaje assembly
es lo más parecido al código de máquina que existe. Por ende, si queremos entender realmente que hace
un lenguaje de programación, necesitamos conocer el lenguaje assembly.</p> 

<p> En ese sentido, el lenguaje assembly es la forma más directa de comunicación que podemos 
tener con una computadora. Con assembly, un programador puede controlar detalladamente el flujo 
de ejecución y de datos en un programa. Además, si tenemos un programa compilado pero no podemos 
acceder al código fuente, la única manera de ver que hace el programa es examinando el archivo 
binario ejecutable. En general es muy difícil o casi imposible volver a traducir el código binario 
al lenguaje original. Entonces sólo podremos traducirlo a assembly y verlo en este lenguaje de bajo nivel.</p>
""",UIConfig.disableAll)

,TutorialStep("Aplicaciones del assembly"
        ,"""
          <p> Desarrollar programas en assembly en general lleva más tiempo que en lenguajes de alto nivel.
Por ende, generalmente no es nuestra primera opción al comenzar un proyecto de software. No obstante,
resulta de gran utilidad saber un poco acerca del mismo. En este tutorial te enseñaremos las bases
del lenguaje assembly.</p>

          <p>Para algunas tareas de programación como desarrollar módulos del sistema operativo,
controladores o el software de arranque de una computadora, el lenguaje assembly muchas veces es nuestra
única opción debido a que necesitamos acceder a funcionalidades de bajo nivel o de hardware.
</p> 

<p> Para algunas aplicaciones particulares, un algoritmo implementado en assembly puede ser más
eficiente y utilizar menos recursos que el mismo algoritmo implementado en un lenguaje de alto nivel.
Esto es especialmente cierto cuando los fabricantes de los procesadores agregar capacidades nuevas
a la CPU que los compiladores de los lenguajes de alto nivel todavía no soportan. </p>
""",UIConfig.disableAll)


,TutorialStep("Resumen"
,"""
  <p>Repasemos lo que hemos visto hasta ahora:</p>
 <p>Assembly es un lenguaje programación de bajo nivel.</p>
 <p>Las instrucciones en assembly se asemejan a las de código de máquina pero son 
 más fáciles de leer y escribir.</p>

""",UIConfig.disableAll,Some(""))


,TutorialStep("A continuación"
,"""
 <p>Ahora que conoces por qué es interesante aprender Assembly, podés <a href="?tutorial=vonsim">
 aprender a utilizar el simulador VonSim</a>.</p>
 
""",UIConfig.disableAll,Some(""))

)
}