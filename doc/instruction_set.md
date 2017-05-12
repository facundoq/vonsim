# Instruction set design 

General format:

| Op code (1 byte) | operands (1+ bytes)|


Zeroary:
all instructions: | IIIIII-- | 

Unary:
jump instructions:   | IIIIII-- | address (2 bytes) |
alu instructions:    | IIIIII-- | operand (1 or 2 bytes) |
stack instructions:  | IIIIII-- | operand (1 byte) |

Binary:

alu/io instructions:  | IIIIII-- | operand (1 or 2 bytes) | operand (1 or 2 bytes) |


## Instructions 
binary (11) : add,adc,sub,sbb,cmp,mov,or,xor,and,in,out
unary (17): inc,dec,not,net,call,jmp,jc,jnc,jz,jnz,jo,jno,js,jns
zeroary (8): pushf,popf,ret,iret,nop,hlt,cli,sti

## Binary instructions 

Format: 
0IIII XXX

Where 0IIII is a 5-bit instruction code and XXX is a 3-bit addressing mode code

add 00000 XXX
adc 00001 XXX
sub 00010 XXX
sbb 00011 XXX
or  00100 XXX
and 00101 XXX
xor 00110 XXX
cmp 00111 XXX
mov 01000 XXX
in  01001 XXX
out 01010 XXX



### Binary addressing modes (6 -> 3 bits):
(0XX)
reg,reg 000
reg,im  001
reg,mem 010
reg,ind 011

(1XX)
mem,reg 100
mem,im  101

NOTE: `reg,im` is the only valid addressing mode for `in` and `out` instructions

## Unary instructions (17)

### ALU instructions
Format: 
1IIIII XX

inc 100001 XX
dec 100010 XX
not 100011 XX
neg 100100 XX

Addressing modes (XX):
reg 001
mem 010
ind 011

# Stack instructions 

push 10101
pop  10110

int
call
jmp
jc
jnc
jz
jnz
jo
jno
js
jns

zeroary instructions (8)
pushf
popf
ret
iret
nop
hlt
cli
sti

Total= 36 -> 6 bits



Unary addressing modes (push pop):
reg 

Unary addressing modes (int call,jmp, jc,etc):
im 

Biggest instruction: 
binary op (1 byte) + mem address (2 bytes) + immediate (2 bytes)
