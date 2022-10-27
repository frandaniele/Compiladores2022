package primerproyecto;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;

import primerproyecto.declaracionesParser.AnContext;
import primerproyecto.declaracionesParser.AndContext;
import primerproyecto.declaracionesParser.Arit_expContext;
import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.EContext;
import primerproyecto.declaracionesParser.EqualityContext;
import primerproyecto.declaracionesParser.FContext;
import primerproyecto.declaracionesParser.FactorContext;
import primerproyecto.declaracionesParser.I_ifContext;
import primerproyecto.declaracionesParser.IforContext;
import primerproyecto.declaracionesParser.InstruccionContext;
import primerproyecto.declaracionesParser.InstruccionesContext;
import primerproyecto.declaracionesParser.LaContext;
import primerproyecto.declaracionesParser.LandContext;
import primerproyecto.declaracionesParser.LoContext;
import primerproyecto.declaracionesParser.LorContext;
import primerproyecto.declaracionesParser.OContext;
import primerproyecto.declaracionesParser.OalContext;
import primerproyecto.declaracionesParser.OpContext;
import primerproyecto.declaracionesParser.OrContext;
import primerproyecto.declaracionesParser.ProgramaContext;
import primerproyecto.declaracionesParser.RContext;
import primerproyecto.declaracionesParser.RelationContext;
import primerproyecto.declaracionesParser.Sec_elifContext;
import primerproyecto.declaracionesParser.TContext;
import primerproyecto.declaracionesParser.TermContext;

public class Visitor extends declaracionesBaseVisitor<String> {
    private String output = "", first_label;
    private List<ErrorNode> errores;
    private static LinkedList<HashMap<String, Integer>> simbolos;
    private LinkedList<String> operandos;
    
    public Visitor() {
        errores = new ArrayList<ErrorNode>();
        simbolos = new LinkedList<HashMap<String, Integer>>();
        operandos = new LinkedList<String>();
        new Generador();
        readFile();
    }
        
    @Override
    public String visitPrograma(ProgramaContext ctx) {
        visitChildren(ctx);
        writeFile();
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
        output += "\nifz " + operandos.pop() + " goto " + g.getNewLabel();

        visitInstruccion(ctx.instruccion());
        
        
        if(!(ctx.sec_elif().getText().equals(""))) {
            output += "\njmp " + g.getNewLabel();
            output += "\nlbl " + g.getLabel();

            first_label = g.getLabel();

            visitSec_elif(ctx.sec_elif());
        }
        else
            output += "\nlbl " + g.getLabel();
            
        return output;
    }

    @Override
    public String visitSec_elif(Sec_elifContext ctx) {
        Generador g = Generador.getInstance();

        if(ctx.IF() != null) {//es elsif
            visitOal(ctx.oal());
            
            output += "\nifz " + operandos.pop() + " goto " + g.getNewLabel();

            visitInstruccion(ctx.instruccion());

            output += "\njmp " + first_label;
            output += "\nlbl " + g.getLabel();

            if(!(ctx.sec_elif().getText().equals("")))//si hay otro
                visitSec_elif(ctx.sec_elif());
            else
                output += "\nlbl " + first_label;
        }
        else {//es else
            visitInstruccion(ctx.instruccion());
            
            output += "\nlbl " + first_label;
        }

        return output;
    }

    @Override
    public String visitIfor(IforContext ctx) {
        if(!(ctx.asignacion().getText().equals("")))
            visitAsignacion(ctx.asignacion());
        else if(!(ctx.declaracion().getText().equals("")))
            visitDeclaracion(ctx.declaracion());
    
        Generador g = Generador.getInstance();
        output += "\nlbl " + g.getNewLabel();

        if(!(ctx.oal(0).getText().equals(""))) {
            visitOal(ctx.oal(0));
            output += "\nifz " + operandos.pop() + " goto " + g.getNewLabel();
        }
        
        if(!(ctx.oal(1).getText().equals("")))//aca me fijo si hay ++x o x++ y depende eso, lo imprimo antes o despues del visitInstruccion
            visitOal(ctx.oal(1));
        
        String lbl_jmp = g.getLabel(), lbl = g.getLabel();//por for anidado
        
        if(!(ctx.instruccion().getText().equals("")))
            visitInstruccion(ctx.instruccion());

        output += "\njmp " + lbl_jmp;
        output += "\nlbl " + lbl;

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
                output += "\n" + ctx.ID().getText() + " = " + operandos.pop();
            }
        }
        
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
        output += g.getNewVar() + operandos.pop();

        if(ctx.MULT() != null) {
            output += " * ";
        }
        else if(ctx.DIV() != null) {
            output += " / ";
        }
        else if(ctx.MOD() != null) {
            output += " % ";
        }

        if(!(ctx.factor().getText().equals("")))
            visitFactor(ctx.factor());
            
        output += operandos.pop();

        operandos.push(g.getVar());

        if(!(ctx.f().getText().equals("")))
            visitF(ctx.f());
        
        return output;
    }

    @Override
    public String visitFactor(FactorContext ctx) {
        if(ctx.ENTERO() != null) 
            operandos.push(ctx.ENTERO().getText());
        else if(ctx.SYMBOL() != null) {
            char c = ctx.SYMBOL().getText().charAt(1);
            operandos.push(String.valueOf(Integer.valueOf(c)));//obtengo ascii del char y lo vuelvo a pasar a str
        }
        else if(ctx.ID() != null) 
            operandos.push(ctx.ID().getText());
        else if(ctx.op() != null) {
            output += "\n" + ctx.op().ID().getText() + " = " + ctx.op().ID().getText();

            if(ctx.op().OP().getText().equals("++"))
                output += " + 1";
            else
                output += " - 1";

        }
        else if(ctx.oal() != null) {
            output += "oal";
        }
        else if(ctx.fun_call() != null) {
            output += "fun_call";
        }
        
        return output;
    }

    @Override
    public String visitOp(OpContext ctx) {
        visitChildren(ctx);   
        
        return output;
    }

    @Override
    public String visitErrorNode(ErrorNode node) {
        addErrorNode(node);
        
        return output;
    }
    
    public void addErrorNode (ErrorNode node) {
        errores.add(node);
    }
    
    public List<ErrorNode> getErrorNodes () {
        return errores;
    }

    private void printOp(String operator) {
        Generador g = Generador.getInstance();
        output += g.getNewVar();
                
        String op2 = operandos.pop();
                
        output += operandos.pop() + " " + operator + " " + op2;
        
        operandos.push(g.getVar());
    }

    private void visitNoNullChilds(ParserRuleContext first, ParserRuleContext second) {
        if(!(first.getText().equals("")))
            visit(first);

        if(!(second.getText().equals("")))
            visit(second);
    }

    private static void readFile() {
		try {
			List<String> allLines = Files.readAllLines(Paths.get("simbolos"));
			for (String line : allLines) {
                simbolos.add(new HashMap<String, Integer>());
                Scanner scan = new Scanner(line.replace("[", "").replace("]", "")).useDelimiter(",");
                while(scan.hasNext()) {
                    simbolos.getLast().put(scan.next().trim(), 0);
                }
                scan.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    private void writeFile() { 
        FileWriter fichero = null;
        PrintWriter pw = null;
        
        try {
            fichero = new FileWriter("tac");
            pw = new PrintWriter(fichero);

            pw.println(output);
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
        finally {
            try {
                if (null != fichero)
                    fichero.close();
            } 
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}