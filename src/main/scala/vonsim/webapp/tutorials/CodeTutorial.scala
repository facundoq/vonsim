package vonsim.webapp.tutorials

import vonsim.webapp.UIConfig

class CodeTutorial extends Tutorial {
  val title="Instrucciones y registros de VonSim"
  
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
<p> Los registros se llaman _ax, _bx, _cx y dx_. Cada uno guarda un valor de 16 bits (2 bytes).<p> 
<p> Cuando se comienza a ejecutar un programa, el simulador le pone el valor 0 a ambos bytes de estos registros.</p>
<p> Puedes observar su valor en la pantalla del simulador.</p>

""",UIConfig.disableAll,None
)


,TutorialStep("Registros y mov (parte 1)"
,"""
<p> Se puede asignar un valor a un registro con la instrucción <code>mov</code>.</p>

<p> Dicha instrucción tiene la sintaxis <code>mov registro, valor</code></p>
<p> Por ejemplo, <code>mov ax,2</code> pone el valor _2 en el registro _ax, o <code>mov cx,12</code> pone el valor _12 en el registro _cx </p>
<p class="exercise"> Ejecuta el código del editor, y verifica que al registro _ax se le asigna el valor _5.</p>
<p class="exercise"> Agrega una instrucción <code>mov</code> al programa para que el registro _dx tenga el valor _3.</p>
""",UIConfig.enableAll,Some("org 2000h\nmov ax,5\nhlt\nend")
)

,TutorialStep("Registros y mov (parte 2)"
,"""
<p class="exercise"> Escribe un programa que le asigne el valor _16 al registro _ax,
 el valor _16h al registro _bx, el _3A2h al _cx 
 y el _120 al registro _dx.</p>
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
  
<p> Por ejemplo, podemos ponerle el valor 5 a _bx, y luego pasar el valor de _bx a _cx para que ambos valgan 5.<p> 

<p> Para ello, después de <code>mov bx,5</code> debemos ejecutar <code>mov cx,bx</code>.</p>

<p> Es decir, le pasamos a _cx el valor de _bx.</p>

<p class="exercise"> Prueba el código del editor, que hace lo descripto más arriba.</p>
<p class="exercise"> Agrega una línea al programa para copiar también el valor del registro 
bx al registro _dx.</p>
 <p class="exercise"> Ejecuta el programa y verifica que los 
 tres registros (_bx, _cx y _dx) terminan con el mismo valor (5h).</p>
""",UIConfig.enableAll,Some("org 2000h\nmov bx,5\nmov cx,bx\nhlt\nend")
)

,TutorialStep("Registros y mov (parte 4)"
    
    
,"""<p> Entonces la instrucción <code>mov</code> también puede usarse con la sintaxis <code>mov registro, registro</code></p>
  
  <p>Algo que no se puede hacer es mover el valor de un registro hacia un valor</p>
  
<p> Por ejemplo, la instrucción <code>mov 3, bx</code> es inválida.</p> 



<p class="exercise"> Prueba dicho código en el editor de código; el programa no compilará.</p>

<p> Entonces, podemos concluir que la sintaxis <code>mov valor, registro</code> es inválida,
o sea que el orden de los operandos es importante.</p>

""",UIConfig.enableAll,Some("org 2000h\nhlt\nend")
)
  
,TutorialStep("Parte alta y parte baja de un registro."
       
,"""<p> Los registros ocupan 2 bytes o 16 bits. Al byte más significativo se lo llama 
<strong>parte alta</strong>, y al menos significativo, <strong>parte baja</strong>.</p>

<p>Por ejemplo, si _ax tiene el valor 3A4Fh, entonces el byte más significativo o parte alta
vale 34h, y el byte menos significativo o parte baja vale 4Fh.</p>

<p class="exercise"> Lee y prueba el código del editor. ¿Cómo queda almacenado el valor
3A4Fh en el registro _ax? ¿Cuál es la parte alta? ¿Y la baja?
</p>

<p class="answer">
  La parte baja está en la primera fila: dicha fila está etiquetada con <strong>L</strong>, por
  <strong>Low</strong> (bajo en inglés).
  
   La parte baja está en la segunda fila: dicha fila está etiquetada con <strong>H</strong>, por
  <strong>High</strong> (alto en inglés).  
</p>

  
""",UIConfig.enableAll,Some("org 2000h\nmov ax,3A4hh\nhlt\nend")
)

,TutorialStep("Modificando las partes altas y bajas de un registro."
       
,"""<p>En ocasiones, queremos manejar datos que sólo ocupan 8 bits, y no necesitamos
toda la capacidad de un registro completo de 16 bits.</p>

<p>En esos casos podemos acceder o cambiar sólo la parte alta o baja de los registros.</p>

<p> Para cambiar el valor de un registro entero utilizamos los identificadores _ax, _bx, _cx o _dx.
</p>

<p> Pero también podemos cambiar sólo la parte alta o la parte baja utilizando otros identificadores.
Para el registro _ax, podemos utilizar los identificadores _al y _ah, para acceder a la parte 
baja y a la parte alta de al directamente.
</p>

<p class="exercise">
Lee y ejecuta el código del editor. ¿Cómo se modifica el registro _ax?
</p>

<div class="answer">
<p>
El registro _ax termina con el valor 5A94h, ya que en la parte alta (_ah) se cargó el valor 5Ah,
y en la parte baja (_al) se cargó el valor 94h.
</p>

<p>
De la misma forma, podemos acceder a las partes altas y bajas de _bx con _bl y _bh, de _cx con _cl y _ch
y de _dx con _dl y _dh.
</p>
</div>

""",UIConfig.enableAll,Some("org 2000h\nmov ah,5Ah\nmov al,94h\nhlt\nend")
)

,TutorialStep("Independencia de los registros bajos y altos."
       
,"""<p class="exercise">
  El código del editor tiene una instrucción que modifica todo el registro ax de una sola vez,
  y otra que modifica solo su parte baja. Léelo y ejecútalo para ver cuáles
  son los valores finales de los registros.
  </p>

<div class="answer"> 
<p>
El primer <code>mov</code> cambia el valor de todo el registro _ax, tanto de 
su parte alta como de su parte baja. Por otro lado, el segundo <code>mov</code> sólo
cambia la parte baja. La parte alta sigue valiendo 5Ah, es decir, no se modificó.
</p>
<p>
Por ende, podemos decir que cuando se accede directamente a la parte baja de _ax utilizando
el identificador al, en verdad estamos viendo a al como un registro independiente. Es decir,
podríamos considerar que tenemos 4 registros de 16 bits (_ax, _bx, _cx y _dx) u 8 registros de 8 bits
(_al, _ah, _bl, _bh, _cl, _ch, _dl y _dh) según nos convenga cuando escribimos el programa. 
 </p>
</div>

""",UIConfig.enableAll,Some("org 2000h\nmov ax,5AC3h\nmov al,94h\nhlt\nend")
)

,TutorialStep("Modificando las partes altas y bajas de un registro."
       
,"""<p class="exercise">
  Lee el código e intenta determinar cuales serán los valores finales de los registros
  cuando termine el programa. Luego ejecuta el código y comprueba el resultado.
  </p>

""",UIConfig.enableAll,Some("""org 2000h
mov bx,12
mov bh,1Ah
mov ah,5Ah
mov al,94h
mov cx,57h
mov cl,al
mov dl,12h
mov dh,10
mov dx,321h
hlt
end""")
)

,TutorialStep("Instrucción add (sumar)"
,"""<p>Además de la instrucción <code>mov</code>, tenemos la instrucción <code>add</code> 
  (<em>sumar</em> en inglés) que nos permite sumar dos números.</p>
  
<p> La sintaxis de dicha instrucción es <code>add registro, valor</code> <p>

<p> Por ejemplo, para sumarle 3 al registro _ax, escribimos <code>add ax, 3</code>. Si antes tenía el valor 4
ahora tendrá el valor 7. </p>

<p class="exercise">Lee y ejecuta el código del editor; verifica que _ax termina con el valor 7.</p>

<p class="exercise"> Luego agrega dos líneas; una para ponerle el valor 4
 al registro _bx, y otra para sumarle 2.</p>
<div class="answer">
<p> Las líneas a agregar son: </p>
<pre><code>mov bx,4
add bx,2</code></pre>
</div>

""",UIConfig.enableAll,Some("org 2000h\nmov ax,4\nadd ax,3\nhlt\nend")
)

,TutorialStep("Instrucción add con dos registros"
,"""<p>La instrucción add también nos permite sumar dos registros</p>
 <p> Entonces, <code>mov ax,bx</code> suma el valor de _ax y el de _bx<p> 
<p>El resultado queda almacenado en _ax.</p>
<p class="exercise">Lee y ejecuta el código del editor; verifica que _ax termina con el valor 7.</p>

<p class="exercise">Agrega dos líneas al programa para darle el valor 2 a _cx y luego sumarle _cx a _bx.
El valor final del registro _bx debería ser 5. </p>

<div class="answer">
<p> Las líneas a agregar son: </p>
<pre><code>mov cx,2
add bx,cx</code></pre>
</div>

""",UIConfig.enableAll,Some("org 2000h\nmov ax,4\nmov bx,3\nadd ax,bx\nhlt\nend")
)

,TutorialStep("Otras instrucciones aritméticas y lógicas"
,"""<p>También hay otras instrucciones aritméticas y lógicas en el simulador.</p>
<p>Las más comunes son <code>sub</code> (por <em>substract</em>, en inglés), para restar o sustraer,
 y <code>or</code>, <code>xor</code> y <code>and</code> que hacen lo mismo que su equivalente lógico.
<p> 
<p class="exercise">
Lee y ejecuta el código del editor, que utiliza instrucciones lógicas para calcular valores
en los registros _ax, _bx, _cx y _dx. Observa el resultado.
</p>
<p class="answer">
Las instrucciones <code>or</code>, <code>xor</code> y <code>and</code> aplican bit a bit las
operaciones binarias del mismo nombre. La instrucción <code>sub</code> hace lo mismo que el 
<code>add</code> pero restando. 
</p>
""",UIConfig.enableAll,Some("""org 2000h
; sub, resta
mov ax,4
sub ax,3
;or
mov bl,11010010b
or bl,10101010b
;xor
mov cl,11010010b
xor cl,10101010b
;and
mov dl,11010010b
and dl,10101010b
hlt
end""")
)

,TutorialStep("La instrucción <code>not</code>:."
    
    
,"""<p> La instrucción <code>not</code> nos permite invertir el patrón de bits de un registro.</p>

<p> Por ejemplo, si al vale 11011001b, aplicando el not pasa a valer 00100110b.
</p>
  
<p>A diferencia de las otras instrucciones que vimos que son <em>binarias</em>, es decir,
tienen dos operandos, el not es <em>unario</em>, o sea que tiene un solo operando.
Su sintaxis es simplemente <code>not registro</code>.</p>

<p class="exercise"> Lee y ejecuta el código del editor. Verifica el valor final del registro al.</p>

""",UIConfig.enableAll,Some("org 2000h\nmov al, 11011001b\nnot al\nhlt\nend")
)

,TutorialStep("La instrucción <code>neg</code>: multiplicar por -1."
    
,"""<p> La instrucción <code>neg</code> es similar al <code>not</code>, 
  ya que también es unaria.</p>

<p>
Nos permite multiplicar a un número por menos uno, es decir, negarlo.
</p>
  

<p class="exercise">
Lee y ejecuta el código del editor. Verifica que los resultados finales sean correctos.
Recuerda que los números negativos se almacenan codificados en CA2. 
  </p>

""",UIConfig.enableAll,Some("org 2000h\nmov bl,-4\nneg bl\nmov ch,7\nneg ch\nhlt\nend")
)

,TutorialStep("<code>inc</code> y <code>dec</code>" 
    
,"""<p> Cuando sumamos y restamos, algo muy común es que sumemos o restemos uno.</p>

<p>
Por ejemplo, si queremos sumarle 1 al registro _ax y restarle 1 al registro _bl, podemos escribir
<code>add ax,1</code> y <code>sub bl,1</code>.
</p>
 
<p>
Estas operaciones de sumar uno o restar uno son 
muy comunes. Por eso, el simulador tiene instrucciones especiales que se llaman <code>inc</code> 
y <code>dec</code> que hacen justamente eso. La primera incrementa un registro en uno y la otra lo decrementa
</p>
  
<p class="exercise">
Lee y ejecuta el código del editor. Verifica que los resultados finales sean correctos. 
</p> 

""",UIConfig.enableAll,Some("org 2000h\nmov ax,5\ninc ax\nmov bl,20\ndec bl\nhlt\nend\n")
)

,TutorialStep("Realizando cálculos aritméticos"
    
,"""<p> Podemos realizar cálculos con algunas de las instrucciones vistas. </p>

<p class="exercise">
Lee y ejecuta el código del editor, que realiza una cuenta que se guarda finalmente
en el registro _cx. ¿Qué cálculo está realizando?
</p>
  
<p class="answer">
Al ejecutar las dos líneas de asignación inicial, sabemos que _ax=30 y _bx=15. 
Luego se le suma 12 a _ax, por ende _ax=42. Después se le resta
1 a _bx con el <code>dec</code>, entonces _bx=14. Finalmente se le resta _bx a _ax, llegando
a _ax=28. Luego se pasa el valor de _ax a _cx, con lo que _cx=28, que en hexadecimal es 1Ch.
En resumen, hicimos que _cx = (a+12)-(b-1), donde a y b son los valores iniciales de _ax y _bx.
</p> 

""",UIConfig.enableAll,Some("""org 2000h
; asignacion inicial
mov ax,30
mov bx,15
;calculos
add ax,12
dec bx
sub ax,bx
; asignacion del resultado en cx
mov cx,ax
hlt
end""")
)

,TutorialStep("Realizando cálculos aritméticos"
    
,"""

<p class="exercise">
Escribe un programa en base a los valores iniciales de _ax y _bx, calcule
_cx = (b+1)+(a-3), donde a y b son los valores iniciales de _ax y _bx. 
</p>
  
<div class="answer">
<p>Una implementación posible (puede haber otras) para ese cálculo es:</p>
<pre><code>;calculos
inc bx
sub ax,3
add bx,ax
; asignacion del resultado en cx
mov cx,bx</pre></code>
</div> 

""",UIConfig.enableAll,Some("""org 2000h
; asignacion inicial
mov ax,21
mov bx,35
;calculos

; asignacion del resultado en cx

hlt
end""")
)

,TutorialStep("Aplicando máscaras binarias"
    
,"""<p> Podemos aplicar máscaras binarias con algunas de las instrucciones vistas. </p>

<p class="exercise">
Lee y ejecuta el código del editor, que utiliza instrucciones lógicas para aplicar 
máscaras binarias. ¿Qué está calculando?
</p>
  
<div class="answer">

<p>El programa aplica las siguientes máscaras</p>
<pre>    10010111
xor 11110010
    --------
    01100101
and 11111011
    --------
or  01100001
    00000010
    --------
not 01100011
    --------    
    10011100
</pre>
</div> 

""",UIConfig.enableAll,Some("""org 2000h
; asignacion inicial
mov al,10010111b
;mascaras
xor al,11110010b
and al,11111011b
or  al,00000010b
not al
hlt
end""")
)

,TutorialStep("Aplicando máscaras binarias"
    
,"""

<div class="exercise">
<p>
Escribe un programa que aplique varias máscaras al registro _al, con valor inicial 10101010b.
<p>
</p> 
Primero,utilizar un <code>and</code> con la máscara _01111111b para hacer que el bit 7 
(el más significativo) se convierta en 0 y el resto quede igual.
</p>
<p>
Luego aplicar un <code>or</code> con la máscara _00000100b para hacer que el bit 2
se convierta en 1. Después, invierte todos los bits.
</p> 
<p>Finalmente, aplica un xor
con la máscara _11110000b para invertir los 4 bits más significativos y dejar igual los otros.  
</p>
</div>
  
<div class="answer">
<pre>
<code>and ax, 01111111b
or  ax, 00000100b
not ax
xor ax, 11110000b
</code>
</pre>
</div> 

""",UIConfig.enableAll,Some("""org 2000h
; asignacion inicial
mov al,10101010b
;aplicación de máscaras

hlt
end""")
)


,TutorialStep("Resumen"
    
,"""<p> VonSim tiene 4 registros llamados _ax, _bx, _cx y _dx.
  Los registros ocupan 2 bytes o 16 bits. Al byte más significativo se lo llama 
<strong>parte alta</strong>, y al menos significativo, <strong>parte baja</strong></p>

<p> Cada registro puede accederse o modificarse completo utilizando
 _ax, _bx, _cx o _dx como identificador. 
También se pueden utilizar las partes altas y bajas de forma independiente, con
_al, _ah, _bl, _bh, _cl, _ch, _dl y _dh como identificadores.
</p>
  
<p>
Hay varias instrucciones para modificar registros. Tenemos <code>add, sub, inc y dec</code>, para sumar y restar,
<code>or, xor, and y not</code>, para realizar operaciones lógicas, y <code>neg</code> para hacer negativo (o positivo)
 un número.
</p>

 <p>
Dichas instrucciones pueden operar tanto en un registro completo como _ax, como en partes 
del mismo, como _al y _ah.
</p> 

 <p>
Podemos utilizar varias instrucciones aritméticas para realizar cálculos.
Asimismo podemos utilizar varias instrucciones lógicas para aplicar máscaras de bits.
</p> 

""",UIConfig.enableAll,Some("org 2000h\nhlt\nend")
)


)




}