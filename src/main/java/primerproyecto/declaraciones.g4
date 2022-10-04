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
ELSE: 'else';
TIPO: (INT | DOUBLE | CHAR | VOID) ;
INT: 'int';
DOUBLE: 'double';
CHAR: 'char';
VOID: 'void';
PYC: ';';
COMA: ',';
EQ: '=';
SUMA : '+';
RESTA : '-';
MULT : '*';
MOD : '%';
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
BARRAS: '//';
CA: '/*';
CC: '*/';
OP : (SUMA SUMA | RESTA RESTA) ;
ID : [A-Za-z_] [A-Za-z0-9_]* ;
SYMBOL : '\'' [ -~] '\'';

fragment ANY : .*? ;
fragment NONL : ~[\n\r]*;

BLOCKCOMMENT : CA ANY CC -> skip; 

LINECOMMENT : BARRAS NONL -> skip;

programa : { System.out.println("inicio parser\n+++++++++++++++PARSER++++++++++++++++\n"); } instrucciones { System.out.println("\n+++++++++++++++++++++++++++++++++++++\nfin parser"); } EOF ;

instrucciones : instruccion instrucciones
              |
              ;
		
instruccion : declaracion { System.out.println("parser: declaracion ok"); }
            | bloque { System.out.println("parser: bloque ok"); }
            | iwhile { System.out.println("parser: while ok"); }
            | i_if { System.out.println("parser: if ok"); }
            | ireturn { System.out.println("parser: return ok"); }
            | ifor { System.out.println("parser: for ok"); }
            | asignacion PYC { System.out.println("parser: asignacion ok"); }
            | op PYC { System.out.println("parser: op ok"); }
            | funcion { System.out.println("parser: declaracion funcion ok"); }
            | fun_call { System.out.println("parser: llamado funcion ok"); }
	         ;

funcion : TIPO ID PA params PC bloque
        | TIPO ID PA params PC PYC
        ;

fun_call : ID PA secvar PC PYC
         | ID PA PC PYC
         ;

params : TIPO ID sec_params
       |
       ;

sec_params : COMA TIPO ID sec_params
           | 
           ;

bloque : LLA instrucciones LLC ;

iwhile :  WHILE PA oal PC instruccion
       ;

i_if : IF PA oal PC instruccion sec_elif
     ;

sec_elif : ELSE IF PA oal PC instruccion sec_elif
         | ELSE instruccion
         |
         ;

ifor : FOR PA (asignacion | declaracion) oal PYC oal PC instruccion
     | FOR PA (asignacion | declaracion) oal PYC oal PC PYC     
     ;

ireturn : RETURN oal PYC
        | RETURN PYC
        ;

asignacion : ID EQ oal
           ;

declaracion : TIPO secvar PYC 
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
  | MOD  factor f
  | 
  ;