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
import primerproyecto.declaracionesParser.IwhileContext;
import primerproyecto.declaracionesParser.OpContext;
import primerproyecto.declaracionesParser.ParamsContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.PrototipoContext;
import primerproyecto.declaracionesParser.Sec_paramsContext;
import primerproyecto.declaracionesParser.SecvarContext;

public class Escucha extends declaracionesBaseListener {
    private Integer args_dec = 1, cant_args = 0;//para los parametros de funciones
    private Boolean redefinition = false;//para ver si estoy redefiniendo funcion y evito instrucciones
    private Boolean in_function = false, nested = true;//no permito nested funcs
    private Boolean error = false;

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
        if(!(ctx.getParent() instanceof FuncionContext) && !redefinition && !nested)//ya agregue el contexto en la funcion
            agregarContexto();
    }
    
    @Override
    public void exitBloque(BloqueContext ctx) {
        if(!redefinition && !nested)//si es un bloque de una funcion redefinida no hago nada
            eliminarContexto();
    }
    
    @Override
    public void enterIfor(IforContext ctx) {
        if(!redefinition && !nested)
            agregarContexto();
    }
    
    @Override
    public void exitIfor(IforContext ctx) {
        if(!redefinition && !nested)
            eliminarContexto();
    }
    
    @Override
    public void exitIwhile(IwhileContext ctx) {
        if(ctx.oal().getText().equals("")) {
            error = true;
            System.out.println("error: expected expression before ‘)’ token");
        }
    }

    @Override
    public void exitAsignacion(AsignacionContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;
        Id id;

        if(!redefinition && !nested) {
            while((!(prc instanceof DeclaracionContext)) && (!(prc instanceof Fc_paramsContext)) && (!(prc instanceof IforContext)) && prc != null)//si es declaracion o si es null salgo
                prc = prc.getParent();

            if(prc == null || prc instanceof Fc_paramsContext || prc instanceof IforContext) {//instruccion -> asignacion | ifor -> asignacion | fc_params -> asignacion
                if((id = ts.buscarSimbolo(ctx.ID().getText())) != null) 
                    id.setInit(true);
                else {
                    error = true;
                    System.out.println("error: ´" + ctx.ID().getText() + "´ undefined (first use in this function)");
                }
            }
            else if((id = ts.buscarSimboloLocal(ctx.ID().getText())) == null) {//declaracion -> secvar -> asignacion 
                DeclaracionContext p = (DeclaracionContext)prc;
                TipoDato t = getTipo(p.TIPO().getText());

                if(p.TIPO().getText().equals("void")) {
                    error = true;
                    System.out.println("error: variable or field ´" + ctx.ID().getText() + "´ declared void ");
                }
                else 
                    ts.addSimbolo(new Variable(ctx.ID().getText(), t, false, true));
            }
            else {//ya esta definida
                error = true;
                System.out.println("error: redefinition of " + ctx.ID().getText());
            } 
        }
    }
    
    @Override
    public void exitFuncion(FuncionContext ctx) {
        if(!redefinition && !nested) {
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
                    
                    if(!flag_return) {
                        error = true;
                        System.out.println("error: control reaches end of non-void function");
                    }
                }
            }
        }

        if(!nested)
            in_function = false;
    }

    @Override
    public void exitFun_dec(Fun_decContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        Funcion fun = (Funcion)ts.buscarSimboloLocal(ctx.ID().getText());
        redefinition = false;
        nested = false;

        if(!in_function) {
            if(fun == null) {//cuando no existe la funcion en este contexto
                TipoDato t = getTipo(ctx.TIPO().getText());

                Funcion f = new Funcion(ctx.ID().getText(), t, false, false);
                ts.addSimbolo(f);

                if(ctx.getParent() instanceof PrototipoContext) {
                    addArgsToFunAndTS(f, ctx, ts, 0);//prototipo
                    f.setEstado(1);//esta prototipada
                    return;
                }
                else { //es declaracion de funcion, agrego los params y su contexto
                    addArgsToFunAndTS(f, ctx, ts, 1);//inicializacion de funcion SIN prototipo
                    f.setEstado(2);//esta inicializada
                }
            }
            else {//cuando el simbolo ya esta
                if(ctx.getParent() instanceof PrototipoContext)//es prototipo, no pasa nada
                    return; 
                else if(fun.getEstado() == 2) {//ya fue inicializada
                    error = true;
                    System.out.println("error: redefinition of " + ctx.ID().getText());
                    redefinition = true;
                }
                else {//inicializo la funcion
                    addArgsToFunAndTS(fun, ctx, ts, 2);//inicializacion de funcion CON prototipo
                    fun.setEstado(2);//fue inicializada
                }
            }
        }
        else {
            error = true;
            System.out.println("error: ISO C forbids nested functions");
            nested = true;
        }
    }

    @Override
    public void exitSecvar(SecvarContext ctx) {
        TablaSimbolos ts = TablaSimbolos.getInstance();
        ParserRuleContext prc = ctx;

        if(!redefinition && !nested) {//si es el bloque de una funcion redefinida no hago nada
            //declaracion o null salgo
            while((!((prc = prc.getParent()) instanceof DeclaracionContext)) && (prc != null));

            if(ctx.ID() != null) {
                if(prc instanceof DeclaracionContext) {//secvar en declaracion
                    if(ts.buscarSimboloLocal(ctx.ID().getText()) == null) {
                        DeclaracionContext p = (DeclaracionContext)prc;
                        TipoDato t = getTipo(p.TIPO().getText());

                        if(p.TIPO().getText().equals("void")) {
                            error = true;
                            System.out.println("error: variable or field ´" + ctx.ID().getText() + "´ declared void ");
                        }
                        else 
                            ts.addSimbolo(new Variable(ctx.ID().getText(), t, false, false));
                    }
                    else {
                        error = true;
                        System.out.println("Error: redeclaration of " + ctx.ID().getText());
                    }
                }                
            }      
        } 
    }
    
    @Override
    public void exitFactor(FactorContext ctx) {
        if(ctx.ID() != null && !redefinition && !nested) {//si hay un id en una operacion aritmetica logica, se considera usada
            setVarUsed(ctx.ID().getText());
        }
    }

    @Override
    public void exitOp(OpContext ctx) {
        if(!redefinition && !nested)
            setVarUsed(ctx.ID().getText());
    }

    @Override
    public void exitFun_call(Fun_callContext ctx) {
        if(ctx.ID() != null && !redefinition && !nested) {
            TablaSimbolos ts = TablaSimbolos.getInstance();
            Funcion f = (Funcion)ts.buscarSimbolo(ctx.ID().getText());
                        
            if(f != null) {
                if(ctx.getParent() instanceof FactorContext) {//chequeo que no sea una funcion void cuando es asignacion
                    if(f.getTipo().toString().equals("VOID")) {
                        error = true;
                        System.out.println("error: void value not ignored as it ought to be");
                        return;
                    }
                }
                //es una llamada no en asignacion
                if(cant_args != args_dec - 1) {
                    error = true;
                    System.out.println("In function " + ctx.ID().getText() + ": expected " + cant_args + " arguments and got " + (args_dec - 1));
                }
                else if(f.getArgs().size() != 0 && ctx.fc_params() == null) {
                    error = true;
                    System.out.println("In function " + ctx.ID().getText() + ": expected " + f.getArgs().size() + " arguments and got 0");
                }
                else
                    setVarUsed(ctx.ID().getText());
            }
            else {
                error = true;
                System.out.println("warning: implicit declaration of function ´" + ctx.ID().getText() + "´");
            }
        }

        args_dec = 1;
        cant_args = 0;
    }

    @Override
    public void exitFc_params(Fc_paramsContext ctx) {
        if(!redefinition && !nested) {  
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
    private void addArgsToFunAndTS(Funcion f, Fun_decContext ctx, TablaSimbolos ts, Integer prototipo) {
        f.setInit(true);
        if(prototipo != 0) {//0 es prototipo, es decir no agrego contexto
            agregarContexto(); //contexto de la funcion
            in_function = true;
        }

        if(ctx.getChild(3).getChildCount() != 0) {//hay params
            TipoDato t_variable = getTipo(((ParamsContext)ctx.getChild(3)).TIPO().getText());
            if(prototipo != 2) //quiere decir que estoy inicializando una funcion que fue prototipada, asi no repito args
                f.addArg(t_variable);

            if(prototipo != 0) //no agrego al contexto si es prototipo
                ts.addSimbolo(new Variable(((ParamsContext)ctx.getChild(3)).ID().getText(), t_variable, false, true)); //init true xq supuestamente estaria inicializado

            if(ctx.getChild(3).getChild(2).getChildCount() != 0) {//hay secparams
                Sec_paramsContext spc = (Sec_paramsContext)ctx.getChild(3).getChild(2);
                        
                while(spc.getChildCount() != 0) {//itero secparams
                    if(prototipo != 2) {
                        t_variable = getTipo(spc.TIPO().getText());
                        f.addArg(t_variable);
                    }
                    
                    if(prototipo != 0)
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
            else {
                error = true;
                System.out.println("error: ´" + id_name + "´ undefined (first use in this function)");
            }
        }
        else {
            error = true;
            System.out.println("error: ´" + id_name + "´ undeclared (first use in this function)");
        }
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

    public Boolean getError() {
        return error;
    }
}