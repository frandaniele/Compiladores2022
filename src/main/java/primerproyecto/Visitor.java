package primerproyecto;

import java.util.HashMap;
import java.util.LinkedList;

import org.antlr.v4.runtime.ParserRuleContext;

import primerproyecto.declaracionesParser.AnContext;
import primerproyecto.declaracionesParser.AndContext;
import primerproyecto.declaracionesParser.Arit_expContext;
import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.EContext;
import primerproyecto.declaracionesParser.EqualityContext;
import primerproyecto.declaracionesParser.FContext;
import primerproyecto.declaracionesParser.FactorContext;
import primerproyecto.declaracionesParser.Fc_paramsContext;
import primerproyecto.declaracionesParser.Fun_callContext;
import primerproyecto.declaracionesParser.Fun_decContext;
import primerproyecto.declaracionesParser.FuncionContext;
import primerproyecto.declaracionesParser.I_ifContext;
import primerproyecto.declaracionesParser.IforContext;
import primerproyecto.declaracionesParser.InstruccionContext;
import primerproyecto.declaracionesParser.InstruccionesContext;
import primerproyecto.declaracionesParser.IreturnContext;
import primerproyecto.declaracionesParser.IwhileContext;
import primerproyecto.declaracionesParser.LaContext;
import primerproyecto.declaracionesParser.LandContext;
import primerproyecto.declaracionesParser.LoContext;
import primerproyecto.declaracionesParser.LorContext;
import primerproyecto.declaracionesParser.OContext;
import primerproyecto.declaracionesParser.OalContext;
import primerproyecto.declaracionesParser.OpContext;
import primerproyecto.declaracionesParser.OrContext;
import primerproyecto.declaracionesParser.ParamsContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.RContext;
import primerproyecto.declaracionesParser.RelationContext;
import primerproyecto.declaracionesParser.Sec_elifContext;
import primerproyecto.declaracionesParser.Sec_paramsContext;
import primerproyecto.declaracionesParser.TContext;
import primerproyecto.declaracionesParser.TermContext;

public class Visitor extends declaracionesBaseVisitor<String> {
    private String output = "", skip_lbl, op_str;
    private Boolean funcall = false;//para cuando un factor es funcall 
    private static LinkedList<HashMap<String, Integer>> simbolos;
    private LinkedList<String> operandos;
    private HashMap<String, String> returns;
    private Integer op = 0;
    
    public Visitor() {
        FileRW file_handler = new FileRW();
        simbolos = file_handler.readFile("simbolos");
        operandos = new LinkedList<String>();
        returns = new HashMap<String, String>();
        new Generador();
    }
        
    @Override
    public String visitPrograma(ProgramaContext ctx) {
        visitChildren(ctx);

        FileRW fileHandler = new FileRW();
        fileHandler.writeFile("tac", output);

        return output;
    }

    @Override
    public String visitInstrucciones(InstruccionesContext ctx) {
        visitChildren(ctx);
        return output;
    }
    
    @Override
    public String visitInstruccion(InstruccionContext ctx) {
        visitChildren(ctx);
        return output;
    }
    
    @Override
    public String visitI_if(I_ifContext ctx) {
        visitOal(ctx.oal());

        Generador g = Generador.getInstance();
        output += "\n\tifz " + operandos.pop() + " goto " + g.getNewLabel();
        String aux_lbl = g.getLabel();

        visitInstruccion(ctx.instruccion());
        
        if(!(ctx.sec_elif().getText().equals(""))) {
            output += "\n\tjmp " + g.getNewLabel() + "\nlbl " + aux_lbl;
            skip_lbl = g.getLabel();

            visitSec_elif(ctx.sec_elif());
            return output;
        } 
        else //si es solo un if
            output += "\nlbl " + aux_lbl;
            
        return output;
    }

    @Override
    public String visitSec_elif(Sec_elifContext ctx) {
        Generador g = Generador.getInstance();

        if(ctx.IF() != null) {//es elsif
            visitOal(ctx.oal());
            
            if(!(ctx.sec_elif().getText().equals(""))){//si hay otro
                output += "\n\tifz " + operandos.pop() + " goto " + g.getNewLabel();
                String exit_lbl = g.getLabel();

                visitInstruccion(ctx.instruccion());

                output += "\n\tjmp " + skip_lbl + "\nlbl " + exit_lbl;

                visitSec_elif(ctx.sec_elif());
            }
            else {
                output += "\n\tifz " + operandos.pop() + " goto " + skip_lbl;
                visitInstruccion(ctx.instruccion());
                output += "\nlbl " + skip_lbl;
            }
        }
        else {//es else
            visitInstruccion(ctx.instruccion());
            output += "\nlbl " + skip_lbl;
        }

        return output;
    }

    @Override
    public String visitIfor(IforContext ctx) {
        if(ctx.asignacion() != null)
            visitAsignacion(ctx.asignacion());
        else if(ctx.declaracion() != null)
            visitDeclaracion(ctx.declaracion());
    
        Generador g = Generador.getInstance();
        output += "\nlbl " + g.getNewLabel();

        if(!(ctx.oal(0).getText().equals(""))) {
            visitOal(ctx.oal(0));
            output += "\n\tifz " + operandos.pop() + " goto " + g.getNewLabel();
        }
        
        if(!(ctx.oal(1).getText().equals("")))//aca me fijo si hay ++x o x++ y depende eso, lo imprimo antes o despues del visitInstruccion
            visitOal(ctx.oal(1));
        
        String lbl_jmp = g.getLabel(), lbl = g.getLabel();//por for anidado
        
        if(!(ctx.instruccion().getText().equals("")))
            visitInstruccion(ctx.instruccion());

        output += "\n\tjmp " + lbl_jmp + "\nlbl " + lbl;

        return output;
    }

    @Override
    public String visitIwhile(IwhileContext ctx) {
        Generador g = Generador.getInstance();
        output += "\nlbl " + g.getNewLabel();

        visitOal(ctx.oal());
            
        output += "\n\tifz " + operandos.pop() + " goto " + g.getNewLabel();

        String lbl_jmp = g.getLabel(), lbl = g.getLabel();//por for anidado

        if(!(ctx.instruccion().getText().equals("")))
            visitInstruccion(ctx.instruccion());

        output += "\n\tjmp " + lbl_jmp + "\nlbl " + lbl;

        return output;
    }

    @Override
    public String visitFuncion(FuncionContext ctx) {
        Generador g = Generador.getInstance();
        output += "\n";
        
        if(ctx.prototipo() == null) {//si no es el prototipo
            if(!returns.containsKey(ctx.fun_dec().ID().getText())) {//si fue prototipada no genero newlabel
                returns.put(ctx.fun_dec().ID().getText(), g.getNewLabel());
                g.getLabel();
            }

            output += "\nlbl " + returns.get(ctx.fun_dec().ID().getText());//es la etiqueta donde comienza la func en que me encuentro
            
            visitNoNullChilds(ctx.fun_dec(), ctx.bloque());
           
            output += "\n\tret\n";
        }
        else //es prototipo, genero label
            if(!returns.containsKey(ctx.prototipo().fun_dec().ID().getText())) {
                returns.put(ctx.prototipo().fun_dec().ID().getText(), g.getNewLabel());
                g.getLabel();
            }

        output += "\n";

        return output;
    }
    
    @Override
    public String visitFun_dec(Fun_decContext ctx) {
        if(!(ctx.params().getText().equals("")))
            visitParams(ctx.params());
        
        return output;
    }

    @Override
    public String visitParams(ParamsContext ctx) {
        if(!(ctx.sec_params().getText().equals("")))
            visitSec_params(ctx.sec_params());
    
        output += "\n\tpop " + ctx.ID().getText();

        return output;
    }

    @Override
    public String visitSec_params(Sec_paramsContext ctx) {
        if(!(ctx.sec_params().getText().equals("")))
            visitSec_params(ctx.sec_params());
    
        output += "\n\tpop " + ctx.ID().getText();
        
        return output;
    }

    @Override
    public String visitFun_call(Fun_callContext ctx) {
        Generador g = Generador.getInstance();

        output += "\n\tpush " + g.getNewLabel();
        
        if(!(ctx.fc_params().getText().equals("")))
            visitFc_params(ctx.fc_params());

        output += "\n\tjmp " + returns.get(ctx.ID().getText()) + "\nlbl " + g.getLabel();
        
        if(funcall) {
            g.getNewVar();
            String var = g.getVar();
            output += "\n\tpop " + var;
            operandos.push(var);
        }
        funcall = false;

        return output;
    }
    
    public String visitFc_params(Fc_paramsContext ctx) {
        if(ctx.ID() != null)
            output += "\n\tpush " + ctx.ID().getText();
        else if(ctx.SYMBOL() != null) 
            output += "\n\tpush " + String.valueOf(Integer.valueOf(ctx.SYMBOL().getText().charAt(1)));
        else if(ctx.ENTERO() != null)
            output += "\n\tpush " + ctx.ENTERO().getText();
        else if(ctx.oal() != null){
            visitOal(ctx.oal());
            output += "\n\tpush " + operandos.pop();
        }
        else if(ctx.asignacion() != null) {
            visitAsignacion(ctx.asignacion());
            output += "\n\tpush " + ctx.asignacion().ID().getText(); 
        }

        if(ctx.fc_params() != null) 
            if(!(ctx.fc_params().getText().equals("")))
                visitFc_params(ctx.fc_params());
        
        return output;
    }

    @Override
    public String visitIreturn(IreturnContext ctx) {
        if(!(ctx.oal().getText().equals(""))) {
            visitOal(ctx.oal());
            
            output += "\n\tpush " + operandos.pop();
        }
        
        return output;
    }
    
    @Override
    public String visitDeclaracion(DeclaracionContext ctx) {
        visitChildren(ctx);
        return output;
    }

    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        for(HashMap<String, Integer> context : simbolos) {
            if(context.containsKey(ctx.ID().getText())) {
                visitOal(ctx.oal());
               
                if(op == 1) 
                    output += op_str;
                
                output += "\n\t" + ctx.ID().getText() + " = " + operandos.pop();
                
                if(op == 2) 
                    output += op_str;
                
                op = 0;
                op_str = "";

                break;//porque si estaba en 2+ contextos distintos el mismo id lo repetia
            }
        }

        return output;
    }

    @Override
    public String visitOp(OpContext ctx) {
        output += "\n\t" + ctx.ID().getText() + " = " + ctx.ID().getText();

        if(ctx.OP().getText().equals("++"))
            output += " + 1";
        else
            output += " - 1";

        return output;
    }

    @Override
    public String visitOal(OalContext ctx) {
        visitNoNullChilds(ctx.lor(), ctx.lo());
        return output;
    }

    @Override
    public String visitLo(LoContext ctx) {
        visitNoNullChilds(ctx.lor(), ctx.lo());
        printOp(ctx.LOR().getText());

        return output;          
    }

    @Override
    public String visitLor(LorContext ctx) {
        visitNoNullChilds(ctx.land(), ctx.lo());
        return output;
    }
  
    @Override
    public String visitLa(LaContext ctx) {
        visitNoNullChilds(ctx.land(), ctx.la());
        printOp(ctx.LAND().getText());

        return output;
    }

    @Override
    public String visitLand(LandContext ctx) {
        visitNoNullChilds(ctx.or(), ctx.la());
        return output;
    }

    @Override
    public String visitO(OContext ctx) {
        visitNoNullChilds(ctx.or(), ctx.o());
        printOp(ctx.OR().getText());

        return output; 
    }

    @Override
    public String visitOr(OrContext ctx) {
        visitNoNullChilds(ctx.and(), ctx.o());
        return output;
    }

    @Override
    public String visitAn(AnContext ctx) {
        visitNoNullChilds(ctx.and(), ctx.an());
        printOp(ctx.AND().getText());

        return output;
    }

    @Override
    public String visitAnd(AndContext ctx) {
        visitNoNullChilds(ctx.equality(), ctx.an());
        return output;
    }

    @Override
    public String visitE(EContext ctx) {
        visitNoNullChilds(ctx.equality(), ctx.e());
        printOp(ctx.EQUA().getText());

        return output;
    }

    @Override
    public String visitEquality(EqualityContext ctx) {
        visitNoNullChilds(ctx.relation(), ctx.e());
        return output;
    }

    @Override
    public String visitR(RContext ctx) {
        visitNoNullChilds(ctx.arit_exp(), ctx.r());
        printOp(ctx.CMP().getText());

        return output;
    }

    @Override
    public String visitRelation(RelationContext ctx) {
        visitNoNullChilds(ctx.arit_exp(), ctx.r());
        return output;
    }

    @Override
    public String visitArit_exp(Arit_expContext ctx) {
        if(ctx.term().getText().equals("") && !(ctx.t().getText().equals("")))//para asignacion de negativos
            operandos.push("");

        visitNoNullChilds(ctx.term(), ctx.t());
        return output;
    }

    @Override
    public String visitT(TContext ctx) {
        visitNoNullChilds(ctx.term(), ctx.t());

        if(ctx.SUMA() != null) {
            printOp("+");
        }
        else if(ctx.RESTA() != null) {
            printOp("-");
        }

        return output;
    }

    @Override
    public String visitTerm(TermContext ctx) {
        visitNoNullChilds(ctx.factor(), ctx.f());
        return output;
    }

    @Override
    public String visitF(FContext ctx) {
        Generador g = Generador.getInstance();
        String aux = operandos.pop();

        if(!(ctx.factor().getText().equals(""))) 
            visitFactor(ctx.factor());

        if(op == 1)
            output += op_str;

        output += g.getNewVar() + aux;

        if(ctx.MULT() != null) {
            output += " * ";
        }
        else if(ctx.DIV() != null) {
            output += " / ";
        }
        else if(ctx.MOD() != null) {
            output += " % ";
        }

        output += operandos.pop();
        
        if(op == 2)
            output += op_str;
        op = 0;
        op_str = "";

        operandos.push(g.getVar());
        
        if(!(ctx.f().getText().equals("")))
            visitF(ctx.f());
        
        return output;
    }

    @Override
    public String visitFactor(FactorContext ctx) {
        if(ctx.ENTERO() != null) 
            operandos.push(ctx.ENTERO().getText().replaceFirst("^0+(?!$)", ""));//elimino 0s a la izquierda
        else if(ctx.SYMBOL() != null) {
            char c = ctx.SYMBOL().getText().charAt(1);
            operandos.push(String.valueOf(Integer.valueOf(c)));//obtengo ascii del char y lo vuelvo a pasar a str
        }
        else if(ctx.ID() != null) 
            operandos.push(ctx.ID().getText());
        else if(ctx.op() != null) {
            String aux2 = ctx.op().getText();

            if(aux2.charAt(0) == '+' || aux2.charAt(0) == '-')//pre
                op = 1;
            else //post
                op = 2;

            op_str += "\n\t" + ctx.op().ID().getText() + " = " + ctx.op().ID().getText();
    
            if(ctx.op().OP().getText().equals("++"))
                op_str += " + 1";
            else
                op_str += " - 1";

            operandos.push(ctx.op().ID().getText());
        }
        else if(ctx.oal() != null) {
            visitOal(ctx.oal());
        }
        else if(ctx.fun_call() != null) {
            funcall = true;
            visitFun_call(ctx.fun_call());
        }
        
        return output;
    }

    private void printOp(String operator) {
        Generador g = Generador.getInstance();

        if(op == 1)
            output += op_str;

        output += g.getNewVar();
                
        String op2 = operandos.pop();
                
        output += operandos.pop() + " " + operator + " " + op2;
        
        if(op == 2)
            output += op_str;

        op = 0;
        op_str = "";

        operandos.push(g.getVar());
    }

    private void visitNoNullChilds(ParserRuleContext first, ParserRuleContext second) {
        if(!(first.getText().equals("")))
            visit(first);

        if(!(second.getText().equals("")))
            visit(second);
    }
}