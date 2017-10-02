package vonsim.webapp.tutorials

import vonsim.webapp.UIConfig

class VariablesTutorial extends Tutorial {
  val title="Variables en assembly"
  
  val initialCode="""
org 2000h
; código aquí
hlt
end
    """
  
  
  val id="variables"
  
  val steps=List(

            TutorialStep("Introducción"
,"""<p>Sabiendo ya como escribir y ejecutar programas con VonSim, y la estructura básica de un 
  programa en assembly, vamos a comenzar a ver funcionan las variables en Assembly.</p> 

<p>Recordemos que como programadores tenemos dos lugares para guardar 
información: la memoria principal o RAM y los registros.</p>
   
</p>En este tutorial veremos como definir variables en la memoria e inicializarlas.<p> 

""",UIConfig.disableAll,None
)
      

,TutorialStep("Ubicación de las variables"
,"""
<p> Al igual que las instrucciones, las variables también deben ser ubicadas con una sentencia <code>org</code>, como vimos en el tutorial anterior.</p>

<p>Por eso agregamos otra sentencia <code>org</code> al comienzo del programa, en este caso en la dirección <code>5</code> de memoria.</p>

<p> Esto quiere decir que las variables que ahora declaremos debajo de la línea <code>org 5</code>
se ubicarán a partir de la dirección de memoria <code>5</code> </p>

""",UIConfig.enableAll,Some("org 5\n;las variables van aqui\norg 2000h\nhlt\nend")
)

,TutorialStep("Declaración de variables"
,"""
<p> Las variables se declaran con la sintaxis <code>nombre tipo valor_inicial</code>.</p>

<p> Hay dos tipos de variables, las <code>db</code>, que ocupan un byte, 
y las <code>dw</code> que ocupan dos bytes</p>

<p>Entonces, para definir una variable llamada peso 
que ocupe un solo byte y tenga como valor inicial 25h (25 hexadecimal), debemos agregar la línea
<code>peso db 25h</code> debajo de la línea <code>org 5h</code> </p>

<p class="exercise"> Agrega la línea <code>peso db 25h</code> para definir la variable peso con valor 25h. 
Ejecuta el programa para cargar las variables en la memoria.</p>

<p class="exercise"> Busca la celda de memoria con dirección 5h. Debería tener el valor 25 
(sin la h, ya que por defecto el simulador muestra todos los bytes codificados en hexadecimal). </p>

""",UIConfig.enableAll,Some("org 5h\n;las variables van aqui\norg 2000h\nhlt\nend")
)

,TutorialStep("Orden de almacenamiento de las variables (parte 1)"
,"""
<p> La variable <code>peso</code> que declaramos se ubicó en la celda con dirección 5h.</p>

<p> ¿Qué sucede si declaramos otra variable, también de un byte, a continuación?</p>

<p class="exercise"> Agrega la línea <code>temperatura db 15h</code> 
para definir la variable temperatura con valor 15h debajo de la variable peso. 
Ejecuta el programa.</p>

<p class="exercise"> Busca la celda de memoria con dirección 5. Debería tener el valor 25.
Mirá la celda siguiente, con dirección 6. ¿Qué valor tiene? </p>

""",UIConfig.enableAll,Some("org 5h\npeso db 25h\n\norg 2000h\nhlt\nend")
)

,TutorialStep("Orden de almacenamiento de las variables (parte 2)"
,"""
<p> La variable <code>temperatura</code> que declaramos se ubicó 
en la celda con dirección 6. Esto es porque las variables se ubican una tras de otra 
a partir de la dirección indicada en la sentencia <code>org</code> </p>

<p class="exercise"> Intercambia las declaraciones de las variables <code>peso</code> y 
<code>temperatura</code>. Ejecuta el programa y verifica que ahora los valores se invierten, es decir, primero 
se ubica la variable temperatura y luego la variable peso en la memoria.</p>

<p class="exercise"> Agrega otras dos variables de un byte llamadas <code>edad</code> y 
<code>altura</code>, con valores iniciales 3Ah y 4Ch, debajo de la variable peso.  
para definir la variable temperatura con valor 14h debajo de la variable peso. 
Ejecuta el programa.</p>

<p class="exercise"> Busca la celda de memoria con dirección 5. Debería tener el valor 25.
Mirá la celda siguiente, con dirección 6. ¿Qué valor tiene? </p>

""",UIConfig.enableAll,Some("org 5h\npeso db 25h\ntemperatura db 14h\norg 2000h\nhlt\nend")
)

,TutorialStep("Valores de las variables"
,"""
<p> Recién inicializamos la variable con un valor codificado en decimal, pero también podemos hacerlo
con un valor codificado en hexadecimal o binario</p>

<p> Para que el valor esté en hexadecimal, simplemente debemos poner una <em>h</em> al final del mismo.
Entonces 25 es el número 25 en decimal, pero 25h es el 25 en hexadecimal, o sea, el 37 en decimal</p>

<p class="exercise"> Agrega la línea <code>peso db 25h</code> para definir la variable peso con valor 25h. 
Ejecuta el programa y verifica que la celda de memoria con dirección 5 tiene el valor 25h.</p>

<p> Podemos también ingresar un byte en formato binario agregando una <em>b</em> al final del mismo.
Por ejemplo, el valor 00101001b representa al valor 41 en decimal, o 29h</p>

<p class="exercise"> Agrega la línea <code>peso db 00101001b</code> para definir la variable peso con valor 29h. 
Ejecuta el programa y verifica que la celda de memoria con dirección 5 tiene el valor 29h.</p>


""",UIConfig.enableAll,Some("org 5\n;las variables van aqui\norg 2000h\nhlt\nend")
)


,TutorialStep("Variables vs Etiquetas"
,"""
<p> .</p>

""",UIConfig.enableAll,Some("org 2000h\nmov ax,5\nhlt\nend")
)


)




}