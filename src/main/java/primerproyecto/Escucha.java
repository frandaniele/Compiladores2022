package primerproyecto;

import org.antlr.v4.runtime.ParserRuleContext;

import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.FactorContext;
import primerproyecto.declaracionesParser.Fun_callContext;
import primerproyecto.declaracionesParser.Fun_decContext;
import primerproyecto.declaracionesParser.FuncionContext;
import primerproyecto.declaracionesParser.IforContext;
import primerproyecto.declaracionesParser.OpContext;
import primerproyecto.declaracionesParser.ParamsContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.PrototipoContext;
import primerproyecto.declaracionesParser.Sec_paramsContext;
import primerproyecto.declaracionesParser.SecvarContext;

public class Escucha extends declaracionesBaseListener {
    private Integer args_dec = 1, cant_args = 0;//para los parametros de funciones

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
        if(!(ctx.getParent() instanceof FuncionContext))//ya agregue el contexto en la funcion
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
            if((id = ts.buscarSimbolo(ctx.ID().getText())) != null) 
                id.setInit(true);
            else
                System.out.println("error: ´" + ctx.ID().getText() + "´ undefined (first use in this function)");
        }
        else if((id = ts.buscarSimboloLocal(ctx.ID().getText())) == null) {//asignacion con declaracion
            DeclaracionContext p = (DeclaracionContext)prc;
            TipoDato t = getTipo(p.TIPO().getText());

            if(p.TIPO().getText().equals("void"))
                System.out.println("error: variable or field ´" + ctx.ID().getText() + "´ declared void ");
            else 
                ts.addSimbolo(new Variable(ctx.ID().getText(), t, false, true));
        }
        else //ya esta definida
            System.out.println("error: redefinition of " + ctx.ID().getText());
    }
    
    @Override
    public void exitFun_dec(Fun_decContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        Funcion fun = (Funcion)ts.buscarSimboloLocal(ctx.ID().getText());

        if(fun == null) {//cuando no existe la funcion en este contexto
            TipoDato t = getTipo(ctx.TIPO().getText());

            Funcion f = new Funcion(ctx.ID().getText(), t, false, false);
            ts.addSimbolo(f);

            if(ctx.getParent() instanceof PrototipoContext) //es prototipo, solo agrego simbolo
                return;
            else //es declaracion de funcion, agrego los params y su contexto
                addArgsToFunAndTS(f, ctx, ts);
        }
        else {//cuando el simbolo ya esta
            if(fun.getInit()) {//ya fue inicializada
                System.out.println("error: redefinition of " + ctx.ID().getText());
                agregarContexto(); //ver, lo pongo para que no haya errores (el contexto no importa)
                //el problema es cuando entro al bloque las declaraciones no deberian hacerse
            }
            else if(ctx.getParent() instanceof PrototipoContext)//es prototipo, no pasa nada
                return; 
            else //inicializo la funcion
                addArgsToFunAndTS(fun, ctx, ts);
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
                    DeclaracionContext p = (DeclaracionContext)prc;
                    TipoDato t = getTipo(p.TIPO().getText());

                    if(p.TIPO().getText().equals("void"))
                        System.out.println("error: variable or field ´" + ctx.ID().getText() + "´ declared void ");
                    else 
                        ts.addSimbolo(new Variable(ctx.ID().getText(), t, false, false));
                }
                else {
                    System.out.println("Error: redeclaration of " + ctx.ID().getText());
                }
            }
            else if(prc instanceof Fun_callContext) {//secvar en funcall
                Funcion fun = (Funcion)ts.buscarSimbolo(((Fun_callContext) prc).ID().getText());

                if(fun == null)
                    System.out.println("warning: implicit declaration of function ´" + ((Fun_callContext) prc).ID().getText() + "´");
                else {
                    cant_args = fun.getArgs().size();
                    args_dec++;                    
                }

                setVarUsed(ctx.ID().getText());    
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
    public void exitOp(OpContext ctx) {
        setVarUsed(ctx.ID().getText());
    }

    @Override
    public void exitFun_call(Fun_callContext ctx) {
        if(ctx.ID() != null) {//llamo a una funcion, la marco como usada
            if(cant_args == args_dec - 1) 
                setVarUsed(ctx.ID().getText());
            else
                System.out.println("In function " + ctx.ID().getText() + ": expected "+ cant_args + " arguments and got " + (args_dec - 1));
        }

        args_dec = 1;
        cant_args = 0;
    }

    //agrega los parametros de las funciones al objeto funcion y a la tabla de simbolos
    private void addArgsToFunAndTS(Funcion f, Fun_decContext ctx, TablaSimbolos ts) {
        f.setInit(true);
        agregarContexto(); //contexto de la funcion

        if(ctx.getChild(3).getChildCount() != 0) {//hay params
            TipoDato t_variable = getTipo(((ParamsContext)ctx.getChild(3)).TIPO().getText());

            f.addArg(t_variable);
            ts.addSimbolo(new Variable(((ParamsContext)ctx.getChild(3)).ID().getText(), t_variable, false, true)); //init true xq supuestamente estaria inicializado

            if(ctx.getChild(3).getChild(2).getChildCount() != 0) {//hay secparams
                Sec_paramsContext spc = (Sec_paramsContext)ctx.getChild(3).getChild(2);
                        
                while(spc.getChildCount() != 0) {//itero secparams
                    t_variable = getTipo(spc.TIPO().getText());

                    f.addArg(t_variable);
                    ts.addSimbolo(new Variable(spc.ID().getText(), t_variable, false, true));
                            
                    if(spc.getChild(3) instanceof Sec_paramsContext)
                        spc = (Sec_paramsContext)spc.getChild(3);
                    else
                        break;
                }
            }
        }
    }

    private void setVarUsed(String id_name) {
        Id id;
        TablaSimbolos ts = TablaSimbolos.getInstance();

        if((id = ts.buscarSimbolo(id_name)) != null) {
            if(id.getInit()) 
                id.setUsado(true);
            else
                System.out.println("error: ´" + id_name + "´ undefined (first use in this function)");
        }
        else
            System.out.println("error: ´" + id_name + "´ undeclared (first use in this function)");
    }
    
    private TipoDato getTipo(String t) {
        if (t.equals("void"))
            return TipoDato.VOID;
        else if (t.equals("int"))
                return TipoDato.INT;
        else if (t.equals("char"))    
                return TipoDato.CHAR;
        else if (t.equals("double"))    
                return TipoDato.DOUBLE;

        return null;
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