package vonsim.webapp.tutorials

import vonsim.webapp.UIConfig

class BasicTutorial extends Tutorial {
  val title="Assembly con VonSim"
  
  val initialCode="""
org 2
; variables aquí
mi_variable db 1Ah

org 2000h
; código aquí
hlt
end
    """
  
  
  val id="basic"
  
  val steps=List(
TutorialStep("Introducción",
"""<p>VonSim es una herramienta para aprender a programar en el lenguaje assembly.</p>
<p>Si quisieramos programar un procesador sin un lenguaje de programación, 
deberíamos conocer exactamente los códigos binarios que representan cada 
instrucción del procesador, lo cual resultaría engorroso y propenso a errores.</p> 
<p>El lenguaje assembly le da nombres a estos códigos, de manera que para sumar 
podamos escribir "add" en lugar de "01010101", por ejemplo.
Por ende, assembly es el lenguaje de programación de más bajo nivel de abstracción que existe, 
ya que es solo una pequeña capa de abstracción sobre los códigos de instrucción del preprocesador.</p>
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
        ,"""<p>Para algunas tareas de programación como desarrollar módulos del sistema operativo,
controladores o el software de arranque de una computadora, el lenguaje assembly muchas veces es nuestra
única opción debido a que necesitamos acceder a funcionalidades de bajo nivel o de hardware.
</p> 

<p> Para algunas aplicaciones particulares, un algoritmo implementado en assembly puede ser más
eficiente y utilizar menos recursos que el mismo algoritmo implementado en un lenguaje de alto nivel.
Esto es especialmente cierto cuando los fabricantes de los procesadores agregar capacidades nuevas
a la CPU que los compiladores de los lenguajes de alto nivel todavía no soportan. </p>
""",UIConfig.disableAll)

,TutorialStep("Estructura del tutorial"
,"""<p> Desarrollar programas en assembly en general lleva más tiempo que en lenguajes de alto nivel.
Por ende, generalmente no es nuestra primera opción al comenzar un proyecto de software. No obstante,
resulta de gran utilidad saber un poco acerca del mismo. En este tutorial te enseñaremos las bases
del lenguaje assembly.</p>

<p>
A continuación explicaremos como utilizar el simulador, y luego comenzaremos a aprender 
sobre el lenguaje assembly y sus instrucciones.
</p>          
""",UIConfig.disableAll)

,TutorialStep("¿Cómo usar el simulador?"
,"""<p> El simulador VonSim tiene tres partes principales: el editor de código, el panel de control de la
  ejecución, y la visualización de la computadora.</p>
  <p>Al trabajar con VonSim generalmente escribirás un poco de código en el editor, luego lo ejecutarás
  con los controles del panel, y verás el resultado de la ejecución mediante la visualización de la computadora.
  </p>

""",UIConfig.disableAll)


,TutorialStep("Editor de código"
,"""<p>Abajo a la izquierda encontrarás el editor de código assembly.</p>
<p>Este editor tiene una verificación automática de sintáxis y semántica del lenguaje, por lo que
si bien no puede detectar todos los errores posibles de programación, te indicará con una cruz roja
cuando una instrucción es incorrecta o falta algo en el programa.</p>
<p> </p>
""",UIConfig.disableAllButEditor)

,TutorialStep("Controles de ejecución"
,"""<p>Arriba se encuentra el panel de control de la ejecución. </p>
<p> El botón "Ejecución Rápida" permite cargar el código assembly de un programa y ejecutar todo 
el programa hasta su finalización. </p>
<p> El botón "Depurar" permite ejecutar el programa paso a paso, es decir, una instrucción a la vez.
De esta forma forma se puede analizar qué hace cada instrucción del programa.</p>
""",UIConfig.disableAllButControls)

,TutorialStep("Visualización de la computadora"
,"""
<p>A la derecha encontrarás la visualización de la computadora simulada, donde se muestran la CPU y la memoria.</p> 
<p>La CPU tiene varios registros, algunos de propósito general y otros especiales, donde se 
almacena información. </p>
<p> Además, la CPU contiene la ALU, donde se realizan los cálculos aritméticos y lógicos</p>
<p> La memoria muestra la dirección (izquierda) y el valor (derecha) de cada una de las celdas de memoria. Los números
están códificados en hexadecimal.
Tiene como ventaja que hay muchas más celdas para almacenar datos, pero su acceso es más lento que el de los registros.</p>
""",UIConfig.disableAllButMainboard)

,TutorialStep("Cómo ejecutar programas"
,"""<p>La forma más simple de ejecutar un programa es simplemente presionar el botón "Ejecución Rápida".</p>
<p>Prueba hacerlo. Se ejecutará el programa de ejemplo que está en el editor, y la dirección de memoria 0002 tomará el valor 1A.</p>

<p>Si todo sale bien, en el panel de ejecución aparecerá el mensaje "Ejecución Finalizada" en verde.</p>
<p> Ten en cuenta que si el programa contiene errores, no podrás cargarlo y ejecutarlo.</p>

""")

)
  
}