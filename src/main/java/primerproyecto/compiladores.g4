grammar compiladores;

@header {
package primerproyecto;
}

fragment LETRA : [A-Za-z] ;
fragment WS: [ \n\t\r];
fragment LETRAMAYUS : [A-Z] ;
fragment DIGITO : [0-9] ;
fragment M : [Mm] ;
fragment S : [Ss] ;

PA : '(';
PC : ')';

HSOK :  (('0'[4-9] | '10') ':' [0-5][0-9]) | ('03:' ('1'[2-9] | [2-5][0-9])) | ('11:' ([01][0-9] | '2'[0-7])) ;
ANIOS :  (([0-2][0-8] '/' ('0'[0-9] | '1'[0-2]) | ('29' | '30') ('12' | [01][013-9]) | '31' ()) '/' '20' ([01][0-9] | '20'));
//HORA :  (([01] [0-9] ':' [0-5] [0-9] ) | ('2' [0-3] ':' [0-5] [0-9])) ;
FECHA :  (([0-2] [0-9]) | ('3' ('0' | '1'))) '/' (('0' [0-9]) | ('1' [0-2])) '/' [0-9] [0-9] [0-9] [0-9];
PM : WS LETRAMAYUS LETRA* ;
PFO : WS LETRA+ '.';
PPM : WS M+ LETRA* S;
//ID : (LETRA | '_') (LETRA | DIGITO | '_')* ;
//NUMERO : DIGITO+ ;
OTRO : . ;

bp : PA bp PC bp 
   |
   ;

s : PM { System.out.println("PM ->" + $PM.getText() + "<--"); } s
  | HSOK { System.out.println("HORA OK ->" + $HSOK.getText() + "<--"); } s
  | ANIOS { System.out.println("entre 2000 y 2020 ->" + $ANIOS.getText() + "<--"); } s
  //| HORA { System.out.println("HORA NO ->" + $HORA.getText() + "<--"); } s
  | FECHA { System.out.println("FECHA NO ->" + $FECHA.getText() + "<--"); } s
  | PPM { System.out.println("PPM ->" + $PPM.getText() + "<--"); } s
  | PFO { System.out.println("PFO ->" + $PFO.getText() + "<--"); } s
 // | ID { System.out.println("ID ->" + $ID.getText() + "<--"); } s
 // | NUMERO { System.out.println("NUMERO ->" + $NUMERO.getText() + "<--"); } s
  | OTRO s
  | EOF
  ;