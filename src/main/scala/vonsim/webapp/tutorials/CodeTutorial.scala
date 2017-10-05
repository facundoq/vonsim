package vonsim.webapp.tutorials

import vonsim.webapp.UIConfig

class CodeTutorial extends Tutorial {
  val title="Instrucciones de assembly"
  
  val initialCode="""
org 2000h
; código aquí
hlt
end
    """
  
  
  val id="code"
  
  val steps=List(

            TutorialStep("Introducción"
,"""<p>Sabiendo ya como escribir y ejecutar programas con VonSim, y la estructura básica de un 
  programa en assembly, vamos a comenzar a ver las instrucciones más usuales de assembly y las 
  formas de guardar información.</p> 

<p>Comenzaremos viendo y probando los registros de la CPU donde se puede guardar información
  y las instrucciones para manipularlos.</p>
   
</p>A medida que tengamos más conocimiento, vamos a poder hacer programas que sean de utilidad.<p> 

""",UIConfig.disableAll,None
)
      

,TutorialStep("Registros"
,"""
  
<p>En VonSim tenemos dos lugares para almacenar información: la memoria y los registros.
La memoria permite guardar mucha más información, pero el acceso a la misma desde la CPU es 
más lento; en cambio, los registros son pocos pero su acceso es prácticamente instantáneo para la CPU.
</p>
<p>La CPU de VonSim tiene 4 registros de propósito general, es decir, que sirven para cualquier cosa.</p>
<p> Los registros se llaman <code>ax</code>, <code>bx</code>, <code>cx</code> y <code>dx</code>. Cada uno guarda un valor de 16 bits (2 bytes).<p> 
<p> Cuando se comienza a ejecutar un programa, el simulador le pone el valor 0 a ambos bytes de estos registros.</p>
<p> Puedes observar su valor en la pantalla del simulador.</p>

""",UIConfig.disableAll,None
)


,TutorialStep("Registros y mov (parte 1)"
,"""
<p> Se puede asignar un valor a un registro con la instrucción <code>mov</code>.</p>

<p> Dicha instrucción tiene la sintaxis <code>mov registro, valor</code></p>
<p> Por ejemplo, <code>mov ax,2</code> pone el valor 2 en el registro ax, o <code>mov cx,12</code> pone el valor 12 en el registro <code>cx</code> </p>
<p class="exercise"> Ejecuta el código del editor, y verifica que al registro <code>ax</code> se le asigna el valor 5.</p>
<p class="exercise"> Agrega una instrucción <code>mov</code> al programa para que el registro <code>dx</code> tenga el valor 3.</p>
""",UIConfig.enableAll,Some("org 2000h\nmov ax,5\nhlt\nend")
)

,TutorialStep("Registros y mov (parte 2)"
,"""
<p class="exercise"> Escribe un programa que le asigne el valor <code>16</code> al registro <code>ax</code>,
 el valor <code>16h</code> al registro <code>bx</code>, el <code>3A2h</code> al <code>cx</code> 
 y el <code>120</code> al registro <code>dx</code>.</p>
<p>Recuerda que puedes ingresar valores en decimal, hexadecimal o binario, pero el simulador
siempre los muestra codificados en hexadecimal.</p>
<div class="answer"><p>El código a ingresar es:</p>
<pre><code>mov ax,16
mov bx,16h
mov cx 3a2h
mov dx,120 
</code></pre>
</div> 
""",UIConfig.enableAll,Some("org 2000h\n\nhlt\nend")
)

,TutorialStep("Registros y mov (parte 3)"
,"""<p>También se puede mover el valor de un registro a otro registro</p>
  
<p> Por ejemplo, podemos ponerle el valor 5 a bx, y luego pasar el valor de bx a cx para que ambos valgan 5.<p> 

<p> Para ello, después de <code>mov bx,5</code> debemos ejecutar <code>mov cx,bx</code>.</p>

<p> Es decir, le pasamos a cx el valor de bx.</p>

<p class="exercise"> Prueba el código del editor, que hace lo descripto más arriba.</p>
<p class="exercise"> Agrega una línea al programa para copiar también el valor del registro 
bx al registro dx.</p>
 <p class="exercise"> Ejecuta el programa y verifica que los tres registros (<code>bx,cx y dx</code>) terminan con el mismo valor (5).</p>
""",UIConfig.enableAll,Some("org 2000h\nmov bx,5\nmov cx,bx\nhlt\nend")
)

,TutorialStep("Registros y mov (parte 4)"
    
    
,"""<p> Entonces la instrucción <code>mov</code> también puede usarse con la sintaxis <code>mov registro, registro</code></p>
  
  <p>Algo que no se puede hacer es mover el valor de un registro hacia un valor</p>
  
<p> Por ejemplo, la instrucción <code>mov 3, bx</code> es inválida.<p> 

<p> O sea, en general, la sintaxis <code>mov valor, registro</code> es inválida.
O sea, el orden de los operandos es importante</p>

<p class="exercise"> Prueba dicho código en el editor de código; el programa no compilará.</p>

""",UIConfig.enableAll,Some("org 2000h\nhlt\nend")
)
  
,TutorialStep("Instrucción add (parte 1)"
,"""<p>La instrucción add nos permite sumar dos números.</p>
  
<p> La sintaxis de dicha instrucción es <code>add registro, valor</code> <p>

<p> Por ejemplo, para sumarle 3 al registro ax, escribimos <code>add ax, 3</code>. Si antes tenía el valor 4
ahora tendrá el valor 7. </p>

<p class="exercise">Lee y ejecuta el código del editor; verifica que ax termina con el valor 7.</p>

<p class="exercise"> Luego agrega dos líneas; una para ponerle el valor 4
 al registro bx, y otra para sumarle 2.</p>


""",UIConfig.enableAll,Some("org 2000h\nmov ax,4\nadd ax,3\nhlt\nend")
)

,TutorialStep("Instrucción add (parte 2)"
,"""<p>La instrucción add también nos permite sumar dos registros</p>
 <p> Entonces, <code>mov ax,bx</code> suma el valor de ax y el de bx<p> 
<p>El resultado queda almacenado en ax.</p>
<p class="exercise">Lee y ejecuta el código del editor; verifica que ax termina con el valor 7.</p>

<p class="exercise">Agrega dos líneas al programa para darle el valor 2 a cx y luego sumarle cx a bx.
El valor final del registro bx debería ser 5. </p>


""",UIConfig.enableAll,Some("org 2000h\nmov ax,4\nmov bx,3\nadd ax,bx\nhlt\nend")
)


,TutorialStep("Instrucción add (parte 2)"
,"""<p>La instrucción add también nos permite sumar dos registros</p>
 <p> Entonces, <code>mov ax,bx</code> suma el valor de ax y el de bx<p> 
<p>El resultado queda almacenado en ax.</p>

""",UIConfig.enableAll,Some("org 2000h\nmov ax,4\nadd ax,3\nadd ax,bx\nhlt\nend")
)

)




}