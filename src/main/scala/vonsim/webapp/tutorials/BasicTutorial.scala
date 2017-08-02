package vonsim.webapp.tutorials

class BasicTutorial extends Tutorial {
  val title="VonSim: Programación en Assembly"
  val disableAllConfig=new TutorialStepConfig(true,true,true)
  val id="basic"
  
  val steps=List(
    TutorialStep("Introducción",
        """VonSim es una herramienta para aprender a programar en el lenguaje assembly.
Si quisieramos programar un procesador sin un lenguaje de programación, deberíamos conocer exactamente los códigos binarios que representan cada instrucción del procesador. 
El lenguaje assembly le da nombres a estos códigos, de manera que para sumar podamos escribir "add" en lugar de "01010101", por ejemplo.
Por ende, assembly es el lenguaje de programación de más bajo nivel de abstracción que existe, ya que es solo una pequeña capa de abstracción sobre los códigos de instrucción del preprocesador.
""")
    ,TutorialStep("¿Por qué aprender assembly?"
        ,"""El lenguaje assembly es una de las herramientas más antiguas de los programadores.
No obstante, hoy en día puede realizarse casi cualquier tipo de software sin siquiera tener que mirar una sola línea de código assembly.
¿Por qué aprenderlo, entonces? El lenguaje assembly 
          
          Si bien puede que no sea """

        )
//"Assembly is among some of the oldest tools in a computer-programmer's toolbox. Entire software projects can be written without ever looking at a single line of assembly code. So the question arises: why learn assembly? Assembly language is one of the closest forms of communication that humans can engage in with a computer. With assembly, the programmer can precisely track the flow of data and execution in a program in a mostly human-readable form. Once a program has been compiled, it is difficult (and at times, nearly impossible) to reverse-engineer the code into its original form. As a result, if you wish to examine a program that is already compiled but would rather not stare at hexadecimal or binary, you will need to examine it in assembly language. Since debuggers will frequently only show program code in assembly language, this provides one of many benefits for learning the language.Assembly language is also the preferred tool, if not the only tool, for implementing some low-level tasks, such as bootloaders and low-level kernel components. Code written in assembly has less overhead than code written in high-level languages, so assembly code frequently will run much faster than equivalent programs written in other languages. Also, code that is written in a high-level language can be compiled into assembly and "hand optimized" to squeeze every last bit of speed out of it. As hardware manufacturers such as Intel and AMD add new features and new instructions to their processors, often times the only way to access those features is to use assembly routines. That is, at least until the major compiler vendors add support for those features. Developing a program in assembly can be a very time consuming process, however. While it might not be a good idea to write new projects in assembly language, it is certainly valuable to know a little bit about it."        
  )
  
}