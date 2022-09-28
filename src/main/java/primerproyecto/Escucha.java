package primerproyecto;

import org.antlr.v4.runtime.tree.TerminalNode;

import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.ProgramaContext;

public class Escucha extends declaracionesBaseListener {
    
    @Override
    public void enterPrograma(ProgramaContext ctx) {
        System.out.println("Comienza compilacion");
    }

    @Override
    public void exitPrograma(ProgramaContext ctx) {
        System.out.println("fin compilacion");
    }

    @Override
    public void enterBloque(BloqueContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.addContext();
    }

    @Override
    public void exitBloque(BloqueContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.delContext();
    }



    @Override
    public void visitTerminal(TerminalNode node) {
        //System.out.println(" -- TOKEN --> |" + node.getText() + "|");
    }
}
/*
 * tabla de simbolos
 * List<Map<String,Id>>
 * cada mapa es segun un contexto, abro una llave creo un contexto, la cierro lo elimino
 * hacerla en singleton
 * addContext
 * delContext
 * addSimbolo(Id id)
 * buscarSimbolo(Id id)
 * buscarSimboloLocal(Id id) -> primero entro con esta, desp la de arriba y sino encuentra marca el error
 * 
 * clase Id 
 *  string nombre
 *  tipodato tipo (void, int, double, char) -> enum TipoDato
 *  boolean inicializado 
 *  boolean usado -> si nunca estuvo a la derecha de una asignacion o no se imprimio no fue usada
 *  setters y getters
 * 
 * funcion extends id -> el retorno seria el tipodato de Id
 *  List<TipoDato> args
 *  addArg(TipoDato td)
 * 
 * variable extends id (la replica)
 */