grammar declaraciones;

@header {
package primerproyecto;
}

ENTERO : '-'? [0-9]+ ;
WS: [ \n\t\r] -> skip;

RETURN : 'return';
FOR : 'for';
WHILE: 'while';
IF: 'if';
INT: 'int';
DOUBLE: 'double';
CHAR: 'char';
PYC: ';';
COMA: ',';
EQ: '=';
SUMA : '+';
RESTA : '-';
MULT : '*';
DIV : '/';
PA : '(';
PC : ')';
LLA : '{';
LLC : '}';
CMP : (MAY | MEN | MAY EQ | MEN EQ) ;
EQUA : (EQ EQ | NOT EQ) ;
MAY: '>';
MEN: '<';
LAND: AND AND;
LOR: OR OR;
AND: '&';//como sumas
OR: '|';//como multiplicaciones
NOT: '!';
OP : (SUMA SUMA | RESTA RESTA) ;
ID : [A-Za-z_] [A-Za-z0-9_]* ;
SYMBOL : '\'' [ -~] '\'';

OTRO : . ;

programa : { System.out.println("inicio"); } instrucciones { System.out.println("fin"); } EOF ;

instrucciones : instruccion instrucciones
              |
              ;
		
instruccion : declaracion { System.out.println("declaracion ok"); }
            | bloque { System.out.println("bloque ok"); }
            | iwhile { System.out.println("while ok"); }
            | i_if { System.out.println("if ok"); }
            | ireturn { System.out.println("return ok"); }
            | ifor { System.out.println("for ok"); }
            | asignacion PYC { System.out.println("asignacion ok"); }
            | op PYC { System.out.println("op ok"); }
	       ;

bloque : LLA instrucciones LLC ;

iwhile :  WHILE PA oal PC instruccion
       ;

i_if : IF PA oal PC instruccion 
     ;

ifor : FOR PA (asignacion | declaracion) oal PYC oal PC instruccion
     | FOR PA (asignacion | declaracion) oal PYC oal PC PYC     
     ;

ireturn : RETURN oal PYC
        ;

asignacion : ID EQ oal
           ;

declaracion : INT secvar PYC 
            | DOUBLE secvar PYC 
            | CHAR secvar PYC 
            | OTRO
	       ;

secvar : ID COMA secvar
       | ID
       | asignacion COMA secvar
       | asignacion
       ;

oal : lor lo
    |
    ;

lor : land lo
    |
    ;

lo : LOR lor lo
   |
   ; 

land : or la
     |
     ;

la  : LAND land la
    |
    ;

or : and o
   |
   ;

o : OR or o
  |
  ;

and : equality an
    |
    ;

an : AND and an
   |
   ;

equality : relation e
         |
         ;

e : EQUA equality e
  |
  ;

relation : arit_exp r
         |
         ; 

r  : CMP arit_exp r //debo atenderlo despues de todas las operaciones, y desp de estas las && y || 
   |
   ;

arit_exp : term t
         |
         ;

term : factor f
     |
     ;

t : SUMA term t 
  | RESTA term t
  |
  ;

op : OP ID //i++, ++i, --i, i--
   | ID OP
   ;  

factor : ENTERO
       | ID //puede haber una variable
       | SYMBOL //creo que se puede hacer aritmetica con simbolos
       | op 
       | PA oal PC //arme un termino con parentesis (vuelvo a empezar)
       ;
    
f : MULT factor f 
  | DIV  factor f
  | 
  ;