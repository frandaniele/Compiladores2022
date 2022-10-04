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
        agregarContexto(); //contexto global?
    }

    @Override
    public void exitPrograma(ProgramaContext ctx) {
        eliminarContexto();//global
        System.out.println("\n*************************************\nfin compilacion");
    }
    
    @Override
    public void enterBloque(BloqueContext ctx) {
        agregarContexto();
    }
    
    @Override
    public void exitBloque(BloqueContext ctx) {
        eliminarContexto();
    }
    
    @Override
    public void enterIfor(IforContext ctx) {
        agregarContexto();
    }
    
    @Override
    public void exitIfor(IforContext ctx) {
        eliminarContexto();
    }
    
    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;
        Id id;

        while((!((prc = prc.getParent()) instanceof DeclaracionContext)) && prc != null);//si es declaracion o si es null salgo

        if(prc == null) {//es una asignacion sola
            if((id = ts.buscarSimboloLocal(ctx.ID().getText())) != null) {
                id.setInit(true);
                return;
            }

            if((id = ts.buscarSimbolo(ctx.ID().getText())) != null) {
                id.setInit(true);
                return;
            }

            System.out.println("listener: variable " + ctx.ID().getText() + " not declared");
            return;
        }

        //asignacion con declaracion
        if((id = ts.buscarSimboloLocal(ctx.ID().getText())) == null) {
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
        ParserRuleContext prc = ctx;

        //declaracion funcall o null salgo, xq solo me interesa si viene de alguna de esas
        while((!((prc = prc.getParent()) instanceof DeclaracionContext)) && !(prc instanceof Fun_callContext)  && (prc != null));

        if(ctx.ID() != null) {
            if(prc instanceof DeclaracionContext) {//secvar en declaracion
                if(ts.buscarSimboloLocal(ctx.ID().getText()) == null) {
                    ts.addSimbolo(new Variable(ctx.ID().getText(), TipoDato.INT, false, false));
                    return;
                }
                else {
                    System.out.println("listener: variable " + ctx.ID().getText() + " redeclared");
                    return;
                }
            }
            
            if(prc instanceof Fun_callContext) {//secvar en funcall
                setVarUsed(ctx.ID().getText());
                return;
            }
        }       
    }

    @Override
    public void exitFactor(FactorContext ctx) {
        if(ctx.ID() != null) {//si hay un id en una operacion aritmetica logica, se considera usada
            setVarUsed(ctx.ID().getText());
        }
    }

    @Override
    public void exitFun_call(Fun_callContext ctx) {
        if(ctx.ID() != null) {//llamo a una funcion, la marco como usada
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
        Id id;
        TablaSimbolos ts = TablaSimbolos.getInstance();

        if((id = ts.buscarSimboloLocal(id_name)) != null) {
            if(id.getInit()) {
                id.setUsado(true);
                return;
            }
        }
        else if((id = ts.buscarSimbolo(id_name)) != null) {
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

    private void agregarContexto() {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.addContext();
    }
    
    private void eliminarContexto() {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ts.delContext();
    }
}