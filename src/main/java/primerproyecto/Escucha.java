package primerproyecto;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.FactorContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.SecvarContext;

public class Escucha extends declaracionesBaseListener {
    
    @Override
    public void enterPrograma(ProgramaContext ctx) {
        System.out.println("Comienza compilacion");
        new TablaSimbolos();
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
        Variable id;

        while(!((prc = prc.getParent()) instanceof DeclaracionContext)) {
            if(prc == null) {
                if((id = (Variable)ts.buscarSimboloLocal(ctx.ID().getText())) == null) {
                    if((id = (Variable)ts.buscarSimbolo(ctx.ID().getText())) == null) {
                        System.out.println("variable " + ctx.ID().getText() + " not declared");
                        return;
                    }
                }
                else {
                    id.setInit(true);
                    return;
                }
            }
        }

        if((id = (Variable)ts.buscarSimboloLocal(ctx.ID().getText())) == null) {
            ts.addSimbolo(new Variable(ctx.ID().getText(), TipoDato.INT, false, true));
            return;
        }
        else {
            System.out.println("variable " + ctx.ID().getText() + " redefined");
            return;
        }            
    }

    @Override
    public void enterFactor(FactorContext ctx) {
    }

    @Override
    public void exitFactor(FactorContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        Variable id;

        if(ctx.ID() != null) {
            if((id = (Variable)ts.buscarSimboloLocal(ctx.ID().getText())) != null) {
                if(id.getInit()) {
                    id.setUsado(true);
                    return;
                }
            }
            else if((id = (Variable)ts.buscarSimbolo(ctx.ID().getText())) != null) {
                if(id.getInit()) {
                    id.setUsado(true);
                    return;
                }
            }
                
            System.out.println("variable " + ctx.ID().getText() + " not defined");
            return;
        }
    }

    @Override
    public void enterSecvar(SecvarContext ctx) {
        //System.out.println(" -- secvar in --> |" + ctx.ID() + "|");
    }

    @Override
    public void exitSecvar(SecvarContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        
        if(ctx.ID() != null) {
            if(ts.buscarSimboloLocal(ctx.ID().getText()) == null) {
                ts.addSimbolo(new Variable(ctx.ID().getText(), TipoDato.INT, false, false));
                return;
            }
            else {
                System.out.println("variable " + ctx.ID().getText() + " redeclared");
                return;
            }
        }
    }

    @Override
    public void enterDeclaracion(DeclaracionContext ctx) {
        //System.out.println(" -- declaracion in --> |" + ctx.secvar() + "|");
    }
    
    @Override
    public void exitDeclaracion(DeclaracionContext ctx) {
       // System.out.println(" -- declaracion out --> |" + ctx.secvar().getText() + "|");
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