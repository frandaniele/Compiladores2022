grammar tac;

@header {
package primerproyecto;
}

ENTERO : [0-9]+ ('.' [0-9]+)*;
WS: [ \n\t\r] -> skip;

JMP: 'jmp' ;
LBL: 'lbl' ;
RET: 'ret' ;
IFZ: 'ifz' ;
POP: 'pop' ;
PUSH: 'push' ;
GOTO: 'goto' ;

TMP: 't' ENTERO;
ETIQ: 'l' ENTERO;

OPERADOR: (EQQ | GEQ | LEQ | SUMA | RESTA | MULT | DIV | MOD | MAY | MEN | LAND | LOR | AND | OR) ;

SUMA : '+';
RESTA : '-';
MULT : '*';
MOD : '%';
DIV : '/';
EQ: '=';
MAY: '>';
MEN: '<';
GEQ: MAY EQ;
LEQ: MEN EQ;
EQQ: EQ EQ;
LAND: AND AND;
LOR: OR OR;
AND: '&';
OR: '|';

ID : [A-Za-z_] [A-Za-z0-9_]* ;

programa : { System.out.println("\n++++++++++++PARSER TAC+++++++++++++\n"); } bloques_basicos { System.out.println("\n+++++++++++++++++++++++++++++++++++++\n"); } EOF ;

bloques_basicos : bloque_basico bloques_basicos
                |
                ;

bloque_basico :  instrucciones ctrl_instr 
              ;

instrucciones : instruccion instrucciones
              |
              ;
		
instruccion : operacion 
            | pop 
            | push 
            | label 
            ; 

ctrl_instr : jump 
           | RET 
           | if_tac
           ;

operacion : asignacion (ID | TMP | ENTERO) OPERADOR (ID | TMP | ENTERO)
          | asignacion (ID | TMP | ENTERO)
          ; 

asignacion : (ID | TMP) EQ 
           ;

if_tac : IFZ (ID | TMP | ENTERO) GOTO ETIQ
       ;

label : LBL ETIQ
      ;

jump : JMP ETIQ
     ;

pop : POP (ID | TMP | ETIQ)    
    ;

push : PUSH (ID | TMP | ETIQ | ENTERO)
     ;