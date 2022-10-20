package primerproyecto;

import org.antlr.v4.runtime.ParserRuleContext;

import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.FactorContext;
import primerproyecto.declaracionesParser.Fc_paramsContext;
import primerproyecto.declaracionesParser.Fun_callContext;
import primerproyecto.declaracionesParser.Fun_decContext;
import primerproyecto.declaracionesParser.FuncionContext;
import primerproyecto.declaracionesParser.IforContext;
import primerproyecto.declaracionesParser.InstruccionesContext;
import primerproyecto.declaracionesParser.IreturnContext;
import primerproyecto.declaracionesParser.OpContext;
import primerproyecto.declaracionesParser.ParamsContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.PrototipoContext;
import primerproyecto.declaracionesParser.Sec_paramsContext;
import primerproyecto.declaracionesParser.SecvarContext;

public class Escucha extends declaracionesBaseListener {
    private Integer args_dec = 1, cant_args = 0;//para los parametros de funciones
    private Boolean redefinition = false;//para ver si estoy redefiniendo funcion y evito entrar al bloque

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
        if(!(ctx.getParent() instanceof FuncionContext) && !redefinition)//ya agregue el contexto en la funcion
            agregarContexto();
    }
    
    @Override
    public void exitBloque(BloqueContext ctx) {
        if(!redefinition)//si es un bloque de una funcion redefinida no hago nada
            eliminarContexto();
    }
    
    @Override
    public void enterIfor(IforContext ctx) {
        if(!redefinition)
            agregarContexto();
    }
    
    @Override
    public void exitIfor(IforContext ctx) {
        if(!redefinition)
            eliminarContexto();
    }
    
    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;
        Id id;

        if(!redefinition) {
            while((!(prc instanceof DeclaracionContext)) && (!(prc instanceof Fc_paramsContext)) && (!(prc instanceof IforContext)) && prc != null)//si es declaracion o si es null salgo
                prc = prc.getParent();

            if(prc == null || prc instanceof Fc_paramsContext || prc instanceof IforContext) {//instruccion -> asignacion | ifor -> asignacion | fc_params -> asignacion
                if((id = ts.buscarSimbolo(ctx.ID().getText())) != null) 
                    id.setInit(true);
                else
                    System.out.println("error: ´" + ctx.ID().getText() + "´ undefined (first use in this function)");
            }
            else if((id = ts.buscarSimboloLocal(ctx.ID().getText())) == null) {//declaracion -> secvar -> asignacion 
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
    }
    
    @Override
    public void exitFuncion(FuncionContext ctx) {
        if(!redefinition) {
            if(ctx.getChild(0) instanceof Fun_decContext) { //si declaro la funcion y no es void reviso que haya return
                if(!(((Fun_decContext) ctx.getChild(0)).TIPO().getText().equals("void"))) {//no es void
                    Boolean flag_return = false;
                    
                    InstruccionesContext insts = (InstruccionesContext) ctx.getChild(1).getChild(1);//funcion -> bloque -> instrucciones
                    
                    if(insts != null) {
                        while(!(insts.getText().equals(""))) {
                            if(insts.getChild(0).getChild(0) instanceof IreturnContext) //instruccion -> ireturn
                                flag_return = true;

                            insts = (InstruccionesContext) insts.getChild(1);//instrucciones -> instrucciones
                        }
                    }
                    
                    if(!flag_return)
                        System.out.println("error: control reaches end of non-void function");
                }
            }
        }
    }

    @Override
    public void exitFun_dec(Fun_decContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        Funcion fun = (Funcion)ts.buscarSimboloLocal(ctx.ID().getText());
        redefinition = false;

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
            if(ctx.getParent() instanceof PrototipoContext)//es prototipo, no pasa nada
                return; 
            else if(fun.getInit()) {//ya fue inicializada
                System.out.println("error: redefinition of " + ctx.ID().getText());
                redefinition = true;
            }
            else //inicializo la funcion
                addArgsToFunAndTS(fun, ctx, ts);
        }
    }

    @Override
    public void exitSecvar(SecvarContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;

        if(!redefinition) {//si es el bloque de una funcion redefinida no hago nada
            //declaracion o null salgo
            while((!((prc = prc.getParent()) instanceof DeclaracionContext)) && (prc != null));

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
            }      
        } 
    }
    
    @Override
    public void exitFactor(FactorContext ctx) {
        if(ctx.ID() != null && !redefinition) {//si hay un id en una operacion aritmetica logica, se considera usada
            setVarUsed(ctx.ID().getText());
        }
    }

    @Override
    public void exitOp(OpContext ctx) {
        if(!redefinition)
            setVarUsed(ctx.ID().getText());
    }

    @Override
    public void exitFun_call(Fun_callContext ctx) {
        if(ctx.ID() != null && !redefinition) {
            TablaSimbolos ts = TablaSimbolos.getInstance();
            Funcion f = (Funcion)ts.buscarSimbolo(ctx.ID().getText());
                        
            if(f != null) {
                if(ctx.getParent() instanceof FactorContext) {//chequeo que no sea una funcion void cuando es asignacion
                    if(f.getTipo().toString().equals("VOID")) {
                        System.out.println("error: void value not ignored as it ought to be");
                        return;
                    }
                }
                //es una llamada no en asignacion
                if(cant_args != args_dec - 1) 
                    System.out.println("In function " + ctx.ID().getText() + ": expected " + cant_args + " arguments and got " + (args_dec - 1));
                else if(f.getArgs().size() != 0 && ctx.fc_params() == null)
                    System.out.println("In function " + ctx.ID().getText() + ": expected " + f.getArgs().size() + " arguments and got 0");
                else
                    setVarUsed(ctx.ID().getText());
            }
            else
                System.out.println("warning: implicit declaration of function ´" + ctx.ID().getText() + "´");
        }

        args_dec = 1;
        cant_args = 0;
    }

    @Override
    public void exitFc_params(Fc_paramsContext ctx) {
        if(!redefinition) {  
            TablaSimbolos ts = TablaSimbolos.getInstance();
            
            ParserRuleContext prc = ctx;
            while((!((prc = prc.getParent()) instanceof Fun_callContext)) && (prc != null));
            
            Funcion fun = (Funcion)ts.buscarSimbolo(((Fun_callContext) prc).ID().getText());
            if(fun != null) {
                cant_args = fun.getArgs().size();
                if(!(ctx.getText().equals("")))//no es la regla vacia
                    args_dec++;                    
                
                if(ctx.ID() != null)//no es simbolo ni entero
                    setVarUsed(ctx.ID().getText());    
            }
        }
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