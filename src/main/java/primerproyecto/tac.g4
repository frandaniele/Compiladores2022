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

bloque_basico :  instrucciones ctrl_instr { System.out.println("bloque_basico ok"); }
              ;

instrucciones : instruccion instrucciones
              |
              ;
		
instruccion : operacion { System.out.println("operacion ok"); }
            | pop { System.out.println("pop ok"); }
            | push { System.out.println("push ok"); }
            | label { System.out.println("label ok"); }
            ; 

ctrl_instr : jump { System.out.println("jump ok"); }
           | RET { System.out.println("ret ok"); }
           | if_tac { System.out.println("if ok"); }
           ;

operacion : asignacion (ID | TMP | ENTERO) OPERADOR (ID | TMP | ENTERO)
          | asignacion (ID | TMP | ENTERO)
          ; 

asignacion : (ID | TMP) EQ 
           ;

if_tac : IFZ TMP GOTO ETIQ
       ;

label : LBL ETIQ
      ;

jump : JMP ETIQ
     ;

pop : POP (ID | TMP | ETIQ)    
    ;

push : PUSH (ID | TMP | ETIQ)
     ;