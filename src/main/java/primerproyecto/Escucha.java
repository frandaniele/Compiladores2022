package primerproyecto;

import org.antlr.v4.runtime.ParserRuleContext;

import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.FactorContext;
import primerproyecto.declaracionesParser.Fun_callContext;
import primerproyecto.declaracionesParser.FuncionContext;
import primerproyecto.declaracionesParser.IforContext;
import primerproyecto.declaracionesParser.ParamsContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.Sec_paramsContext;
import primerproyecto.declaracionesParser.SecvarContext;

public class Escucha extends declaracionesBaseListener {
    @Override
    public void enterPrograma(ProgramaContext ctx) {
        System.out.println("Comienza compilacion\n*************COMPILACION*************\n");
        new TablaSimbolos();
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.addContext(); //contexto global?
    }

    @Override
    public void exitPrograma(ProgramaContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        System.out.println("\n*************************************\nfin compilacion");
        ts.delContext();//global
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
    public void enterIfor(IforContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.addContext();
    }
    
    @Override
    public void exitIfor(IforContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.delContext();
    }
    
    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;
        Variable id;

        while((!((prc = prc.getParent()) instanceof DeclaracionContext)) && prc != null);//si es declaracion o si es null salgo

        if(prc == null) {//es una asignacion sola
            if((id = (Variable)ts.buscarSimboloLocal(ctx.ID().getText())) != null) {
                id.setInit(true);
                return;
            }

            if((id = (Variable)ts.buscarSimbolo(ctx.ID().getText())) != null) {
                id.setInit(true);
                return;
            }

            System.out.println("listener: variable " + ctx.ID().getText() + " not declared");
            return;
        }

        //asignacion con declaracion
        if((id = (Variable)ts.buscarSimboloLocal(ctx.ID().getText())) == null) {
            ts.addSimbolo(new Variable(ctx.ID().getText(), TipoDato.INT, false, true));
            return;
        }
        else {
            System.out.println("listener: variable " + ctx.ID().getText() + " redefined");
            return;
        }            
    }
    
    @Override
    public void exitFuncion(FuncionContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        
        if(ts.buscarSimboloLocal(ctx.ID().getText()) == null) {
            ts.addSimbolo(new Funcion(ctx.ID().getText(), TipoDato.INT, false, true));
            return;
        }
        else {
            System.out.println("listener: function " + ctx.ID().getText() + " redefined");
            return;
        }            
    }

    @Override
    public void exitSecvar(SecvarContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        
        if(ctx.ID() != null && ctx.getParent() instanceof DeclaracionContext) {//secvar en declaracion
            if(ts.buscarSimboloLocal(ctx.ID().getText()) == null) {
                ts.addSimbolo(new Variable(ctx.ID().getText(), TipoDato.INT, false, false));
                return;
            }
            else {
                System.out.println("listener: variable " + ctx.ID().getText() + " redeclared");
                return;
            }
        }
        
        //secvar en funcall
        ParserRuleContext prc = ctx;
        if(ctx.ID() != null) {
            while((prc = prc.getParent()) != null) {
                if(prc instanceof Fun_callContext) {
                    setVarUsed(ctx.ID().getText());
                }
            }
        }
    }

    @Override
    public void exitFactor(FactorContext ctx) {
        if(ctx.ID() != null) {
            setVarUsed(ctx.ID().getText());
        }
    }

    @Override
    public void exitDeclaracion(DeclaracionContext ctx) {
        //System.out.println("symbol: " + ctx.TIPO().getSymbol().getText() + " " + ctx.TIPO().getText() + ": " + ctx.TIPO().getSymbol().getTokenIndex());
        if(ctx.TIPO().getSymbol().getTokenIndex() - 1 == declaracionesParser.VOID) // revisar
            System.out.println("listener: void variable not allowed");
    }

    @Override
    public void exitParams(ParamsContext ctx) {
    }

    @Override
    public void exitSec_params(Sec_paramsContext ctx) {
    }

    private void setVarUsed(String id_name) {
        Variable id;
        TablaSimbolos ts = TablaSimbolos.getInstance();

        if((id = (Variable)ts.buscarSimboloLocal(id_name)) != null) {
            if(id.getInit()) {
                id.setUsado(true);
                return;
            }
        }
        else if((id = (Variable)ts.buscarSimbolo(id_name)) != null) {
            if(id.getInit()) {
                id.setUsado(true);
                return;
            }
        }
        else{
            System.out.println("listener: variable " + id_name + " not declared");
            return;
        }
        
        System.out.println("listener: variable " + id_name + " not defined");
        return;
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