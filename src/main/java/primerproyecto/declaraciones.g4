grammar declaraciones;

@header {
package primerproyecto;
}

ENTERO : '-'?[0-9] ;
WS: [ \n\t\r] -> skip;

WHILE: 'while';
INT: 'int';
DOUBLE: 'double';
CHAR: 'char';
PYC: ';';
COMA: ',';
GEQ: '>=';
LEQ: '<=';
MAY: '>';
MEN: '<';
EQ: '=';
SUMA : '+';
RESTA : '-';
MULT : '*';
DIV : '/';
PA : '(';
PC : ')';
LLA : '{';
LLC : '}';
CMP : (EQ EQ | MAY | MEN | GEQ | LEQ) ;
ID : [A-Za-z_] [A-Za-z0-9_]* ;
SYMBOL : '\'' [ -~] '\'';

OTRO : . ;

programa : { System.out.println("inicio"); } instrucciones { System.out.println("fin"); } EOF ;

instrucciones  : instruccion instrucciones
               |
               ;
		
instruccion : declaracion
            //| asignacion
            | bloque
            | iwhile
		        ;

bloque : LLA instrucciones LLC ;

iwhile :  WHILE PA oal CMP oal PC bloque
       |  WHILE PA oal PC bloque
       |  WHILE PA oal PC instruccion
       |  WHILE PA oal CMP oal PC instruccion
       ;

//asignacion : INT secas PYC { System.out.println("ok"); }
//           | DOUBLE secas PYC { System.out.println("ok"); }
//           | CHAR secas PYC { System.out.println("ok"); }
//           | OTRO
//           ;

declaracion	: INT secvar PYC { System.out.println("ok"); }
            | DOUBLE secvar PYC { System.out.println("ok"); }
            | CHAR secchar PYC { System.out.println("ok"); }
            | OTRO
		    ;

secvar   : ID COMA secvar
         | ID EQ (ID | ENTERO | oal) COMA secvar
         | ID EQ (ID | ENTERO | oal)
         | ID
         ;

secchar : ID COMA secchar
        | ID EQ (ID | ENTERO | SYMBOL) COMA secchar
        | ID EQ (ID | ENTERO | SYMBOL)
        | ID
        ;

oal : term t
    |
    ;

term : factor f
     |
     ;

t : SUMA term t 
  | RESTA term t
  |
  ;

factor : ENTERO
       | ID //puede haber una variable
       | PA oal PC //arme un termino con parentesis (vuelvo a empezar)
       ;
    
f : MULT factor f 
  | DIV  factor f
  |
  ;