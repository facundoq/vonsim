package vonsim.webapp.tutorials

import vonsim.webapp.UIConfig

class BasicTutorial extends Tutorial {
  val title="Assembly con VonSim"
  
  
  
  
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
        ,"""El lenguaje assembly es una de las herramientas más antiguas de los programadores.
No obstante, hoy en día puede realizarse casi cualquier tipo de software sin siquiera tener que mirar una sola línea de código assembly.
¿Por qué aprenderlo, entonces? El lenguaje assembly 
          
          Si bien puede que no sea 
""",UIConfig.disableAll)

,TutorialStep("Visualización de la computadora"
,"""<p>A la derecha encontrarás la visualización de la computadora simulada.</p> 
<p>La CPU tiene varios registros, algunos de propósito general y otros especiales, donde se 
almacena información. </p>
<p> Además, la CPU contiene la ALU, donde se realizan los cálculos aritméticos y lógicos</p>
<p> La memoria muestra la dirección (izquierda) y el valor (derecha) de cada una de las celdas de memoria.
Tiene como ventaja que hay muchas más celdas para almacenar datos, pero su acceso es más lento que el de los registros.</p>
""",UIConfig.disableAllButMainboard)

,TutorialStep("Editor de código"
,"""<p>A la izquierda encontrarás el editor de código assembly.</p>
<p> </p>
<p> </p>
""",UIConfig.disableAllButEditor)

,TutorialStep("Controles de ejecución"
,"""<p>Arriba se encuentra el panel de control de la ejecución. </p>
<p> </p>
<p> </p>
""",UIConfig.disableAllButControls)

,TutorialStep("Cómo ejecutar programas"
,"""<p>La forma más simple de ejecutar un programa</p>
<p> </p>
<p> </p>
""")

/*"Assembly is among some of the oldest tools in a computer-programmer's toolbox. 
 * Entire software projects can be written without ever looking at a single line of assembly code.
 *  So the question arises: why learn assembly? Assembly language is one of the closest forms of 
 *  communication that humans can engage in with a computer. 
 *  With assembly, the programmer can precisely track the flow of data and execution in a 
 *  program in a mostly human-readable form. Once a program has been compiled, it is difficult 
 *  (and at times, nearly impossible) to reverse-engineer the code into its original form. 
 *  As a result, if you wish to examine a program that is already compiled but would rather 
 *  not stare at hexadecimal or binary, you will need to examine it in assembly language. Since 
 *  debuggers will frequently only show program code in assembly language, this provides one 
 *  of many benefits for learning the language.Assembly language is also the preferred tool, 
 *  if not the only tool, for implementing some low-level tasks, such as bootloaders and low-level 
 *  kernel components. Code written in assembly has less overhead than code written in high-level 
 *  languages, so assembly code frequently will run much faster than equivalent programs written 
 *  in other languages. Also, code that is written in a high-level language can be compiled into 
 *  assembly and "hand optimized" to squeeze every last bit of speed out of it. As hardware 
 *  manufacturers such as Intel and AMD add new features and new instructions to their processors,
 *   often times the only way to access those features is to use assembly routines. That is, at 
 *   least until the major compiler vendors add support for those features. Developing a program 
 *   in assembly can be a very time consuming process, however. While it might not be a good idea 
 *   to write new projects in assembly language, it is certainly valuable to know a little bit 
 *   about it."        
*/  )
  
}