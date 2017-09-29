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
  programa en assembly, vamos a comenzar a ver como definir variables en la memoria.</p> 

<p>Recordemos que como programadores tenemos dos lugares para guardar información: la memoria principal o RAM y los registros.</p>
   
</p>En este tutorial veremos como definir variables en la memoria e inicializarlas.<p> 

""",UIConfig.disableAll,None
)
      

,TutorialStep("Ubicación de las variables"
,"""
<p> Al igual que las instrucciones, las variables también deben ser ubicadas con una sentencia <code>org</code>, como vimos en el tutorial anterior.</p>

<p>Entonces, agregaremos otra .</p>

<p> Dicha instrucción tiene la sintaxis <code>mov registro, valor</code></p>
<p> Por ejemplo, <code>mov ax,2</code> pone el valor 2 en el registro ax, o <code>mov cx,12</code> pone el valor 12 en el registro <code>cx</code> </p>
<p class="exercise"> Ejecuta el código del editor, y verifica que al registro <code>ax</code> se le asigna el valor 5.</p>
<p class="exercise"> Agrega una instrucción <code>mov</code> al programa para que el registro <code>dx</code> tenga el valor 3.</p>
""",UIConfig.enableAll,Some("org 1000h\n;las variables van aqui\norg 2000h\nhlt\nend")
)


,TutorialStep("Variables vs Etiquetas"
,"""
<p> Se puede cambiar el valor de los registros con la instrucción <code>mov</code>.</p>

""",UIConfig.enableAll,Some("org 2000h\nmov ax,5\nhlt\nend")
)


)




}