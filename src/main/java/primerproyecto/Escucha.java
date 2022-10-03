package primerproyecto;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.SecvarContext;

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

    @Override
    public void enterAsignacion(AsignacionContext ctx) {
        //System.out.println(" -- asignacion out --> |" + ctx.ID() + "|" + " Parent: " + ctx.getParent() + "|");
    }
    
    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;

        while(!((prc = prc.getParent()) instanceof DeclaracionContext)) {
            System.out.println(" -- asignacion out --> |" + (prc instanceof DeclaracionContext) + "|");
            if(prc == null){
                ts.buscarSimboloLocal(null);
                
                System.out.println("null");
                return;
            }
        }
            
        Variable id = new Variable(ctx.ID().getText(), TipoDato.INT, false, true);
        ts.addSimbolo(id);
    }

    @Override
    public void enterSecvar(SecvarContext ctx) {
        //System.out.println(" -- secvar in --> |" + ctx.ID() + "|");
    }

    @Override
    public void exitSecvar(SecvarContext ctx) {
        System.out.println(" -- secvar out --> |" + ctx.ID() + "|");
        //algo parecido a la asignacion, pero solo addsimbolo
    }

    @Override
    public void enterDeclaracion(DeclaracionContext ctx) {
        System.out.println(" -- declaracion in --> |" + ctx.secvar() + "|");
    }
    
    @Override
    public void exitDeclaracion(DeclaracionContext ctx) {
        System.out.println(" -- declaracion out --> |" + ctx.secvar().getText() + "|");
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