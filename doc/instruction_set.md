# Instruction set encoding

## Instructions

* binary (11) : `add,adc,sub,sbb,cmp,mov,or,xor,and,in,out`

* unary (17): `inc,dec,not,net,call,jmp,jc,jnc,jz,jnz,jo,jno,js,jns`

* zeroary (8): `pushf,popf,ret,iret,nop,hlt,cli,sti`

## General format

| Op code (1 byte) | addressing mode (optional) | operands (1+ bytes)|

Op code format: `CCCCCCCC`  (8-bit operation Code)

* Zeroary
 * all instructions: `| CCCCCCCC |`

* Unary
  * jump instructions:   `| CCCCCCCC | address (2 bytes) |`
  * alu instructions:    `| CCCCCCCC | -----MMS | operand (1 or 2 bytes) |`
  * stack instructions:  `| CCCCCCCC | operand (1 byte) |`

  * Where:
    * `CCCCCC` = A 8-bit operation Code
    * `MM` = A 2-bit addresing Mode code
    * `S` = A bit indicating if the operand's Size is 1 byte or 2 bytes (0 => 1 byte, 1 => 2 bytes).

* Binary:

  * all instructions:  `| CCCCCCCC | ----MMMS | operand (1 or 2 bytes) | operand (1 or 2 bytes) |`
  * Where:
    `CCCCCC` = A 6-bit operation Code
    `MMM` = A 3-bit addressing Mode code
    `S` = A bit indicating if the operands' Sizes are 1 byte or 2 bytes (0 => 1 byte, 1 => 2 bytes).


## Binary Instructions

### Binary operations
Format: `00 00 CCCC`

Where `CCCC` is a 4-bit instruction code.

```
add 00 00 0000
adc 00 00 0001
sub 00 00 0010
sbb 00 00 0011
or  00 00 0100
and 00 00 0101
xor 00 00 0110
cmp 00 00 0111
mov 00 00 1000
in  00 00 1001
out 00 00 1010
```


### Binary addressing Modes (16 -> 4 bits):
Format: `0000 MMMS`

`S` can be 0 or 1 (0 => 1-byte operands, 1 => 2 bytes operands)

```
reg,reg 0000 000S
reg,im  0000 001S
reg,mem 0000 010S
reg,ind 0000 011S

mem,reg 0000 100S
mem,im  0000 101S
ind,reg 0000 110S
ind,im  0000 111S
```

Where:
* ind: indirect operand
* im:  immediate operand
* reg: register operand
* mem: memory operand

NOTE: `reg,im` is the only valid addressing mode for `in` and `out` instructions

## Unary instructions (17)
General Format: `00 CCCCCC`

### ALU operations
Format: `00 01 CCCC`

```
inc 00 01 0001
dec 00 01 0010
not 00 01 0011
neg 00 01 0100
```

### Unary ALU operation addressing modes (MMS):
Format: `0000 0MMS`

```
reg 0000 000S
mem 0000 001S
ind 0000 010S
```

S can be 0 or 1 (0 => 1-byte operands, 1 => 2 bytes operands)


### Stack instructions
Format: 00 10 000C

```
push 00 10 0000
pop  00 10 0001
```

### Jump instructions

Format: 00 11 CCCC

```
jc    00 11 0000
jnc   00 11 0001
jz    00 11 0010
jnz   00 11 0011
jo    00 11 0100
jno   00 11 0101
js    00 11 0110
jns   00 11 0111
int   00 11 1000
call  00 11 1001
jmp   00 11 1010
```

## Zeroary instructions (8)

Format: `01 00 CCCC`
```
pushf 01 00 0001
popf  01 00 0010
ret   01 00 0011
iret  01 00 0100
nop   01 00 0101
hlt   01 00 0110
cli   01 00 0111
sti   01 00 1000
```


## Sample instruction sizes
```
6 bytes = binary op (1 byte) + addressing mode (1 byte) + mem address (2 bytes) + immediate (2 bytes)
5 bytes = binary op (1 byte) + addressing mode (1 byte) + mem address (2 bytes) + immediate (1 byte )
4 bytes = binary op (1 byte) + addressing mode (1 byte) + register    (1 byte ) + immediate (1 byte )
4 bytes = unary  op (1 byte) + addressing mode (1 byte) + mem address (2 bytes)
3 bytes = unary  op (1 byte) + addressing mode (1 byte) + register    (1 byte )
```
