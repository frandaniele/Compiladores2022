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
import primerproyecto.declaracionesParser.TContext;
import primerproyecto.declaracionesParser.TermContext;

public class Visitor extends declaracionesBaseVisitor<String> {
    private String output = "";
    private List<ErrorNode> errores;
    private static LinkedList<HashMap<String, Integer>> simbolos;
    private LinkedList<String> operandos;
    
    public Visitor() {
        errores = new ArrayList<ErrorNode>();
        simbolos = new LinkedList<HashMap<String, Integer>>();
        operandos = new LinkedList<String>();
        new GeneradorLabels();
        new GeneradorVars();
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
        GeneradorVars gv = GeneradorVars.getInstance();
        gv.reset();
        output += "\n";

        if(!(ctx.lor().getText().equals("")))
            visitLor(ctx.lor());

        if(!(ctx.lo().getText().equals("")))
            visitLo(ctx.lo());

        return output;
    }

    @Override
    public String visitLo(LoContext ctx) {
        if(!(ctx.lor().getText().equals("")))
            visitLor(ctx.lor());

        if(!(ctx.lo().getText().equals("")))
            visitLo(ctx.lo());

        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();
                
        String op2 = operandos.pop();
                
        output += operandos.pop() + " " + ctx.LOR().getText() + " " + op2;
        
        operandos.push(gv.getVar());

        return output;          
    }

    @Override
    public String visitLor(LorContext ctx) {
        if(!(ctx.land().getText().equals("")))
            visitLand(ctx.land());

        if(!(ctx.lo().getText().equals("")))
            visitLo(ctx.lo());

        return output;
    }
  
    @Override
    public String visitLa(LaContext ctx) {
        if(!(ctx.land().getText().equals("")))
            visitLand(ctx.land());

        if(!(ctx.la().getText().equals("")))
            visitLa(ctx.la());
        
        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();
            
        String op2 = operandos.pop();
            
        output += operandos.pop() + " " + ctx.LAND().getText() + " " + op2;
        
        operandos.push(gv.getVar());

        return output;
    }

    @Override
    public String visitLand(LandContext ctx) {
        if(!(ctx.or().getText().equals("")))
            visitOr(ctx.or());

        if(!(ctx.la().getText().equals("")))
            visitLa(ctx.la());

        return output;
    }

    @Override
    public String visitO(OContext ctx) {
        if(!(ctx.or().getText().equals("")))
            visitOr(ctx.or());

        if(!(ctx.o().getText().equals("")))
            visitO(ctx.o());

        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();
                
        String op2 = operandos.pop();
                
        output += operandos.pop() + " " + ctx.OR().getText() + " " + op2;
        
        operandos.push(gv.getVar());

        return output; 
    }

    @Override
    public String visitOr(OrContext ctx) {
        if(!(ctx.and().getText().equals("")))
            visitAnd(ctx.and());

        if(!(ctx.o().getText().equals("")))
            visitO(ctx.o());

        return output;
    }

    @Override
    public String visitAn(AnContext ctx) {
        if(!(ctx.and().getText().equals("")))
            visitAnd(ctx.and());

        if(!(ctx.an().getText().equals("")))
            visitAn(ctx.an());

        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();
            
        String op2 = operandos.pop();
            
        output += operandos.pop() + " " + ctx.AND().getText() + " " + op2;
        
        operandos.push(gv.getVar());

        return output;
    }

    @Override
    public String visitAnd(AndContext ctx) {
        if(!(ctx.equality().getText().equals("")))
            visitEquality(ctx.equality());

        if(!(ctx.an().getText().equals("")))
            visitAn(ctx.an());

        return output;
    }

    @Override
    public String visitE(EContext ctx) {
        if(!(ctx.equality().getText().equals("")))
            visitEquality(ctx.equality());

        if(!(ctx.e().getText().equals("")))
            visitE(ctx.e());
            
        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();
        
        String op2 = operandos.pop();
        
        output += operandos.pop() + " " + ctx.EQUA().getText() + " " + op2;
        
        operandos.push(gv.getVar());

        return output;
    }

    @Override
    public String visitEquality(EqualityContext ctx) {
        if(!(ctx.relation().getText().equals("")))
            visitRelation(ctx.relation());

        if(!(ctx.e().getText().equals("")))
            visitE(ctx.e());

        return output;
    }

    @Override
    public String visitR(RContext ctx) {
        if(!(ctx.arit_exp().getText().equals("")))
            visitArit_exp(ctx.arit_exp());

        if(!(ctx.r().getText().equals("")))
            visitR(ctx.r());
            
        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();
    
        String op2 = operandos.pop();
    
        output += operandos.pop() + " " + ctx.CMP().getText() + " " + op2;
        
        operandos.push(gv.getVar());

        return output;
    }

    @Override
    public String visitRelation(RelationContext ctx) {
        if(!(ctx.arit_exp().getText().equals("")))
            visitArit_exp(ctx.arit_exp());

        if(!(ctx.r().getText().equals("")))
            visitR(ctx.r());

        return output;
    }

    @Override
    public String visitArit_exp(Arit_expContext ctx) {
        if(!(ctx.term().getText().equals("")))
            visitTerm(ctx.term());

        if(!(ctx.t().getText().equals("")))
            visitT(ctx.t());

        return output;
    }

    @Override
    public String visitT(TContext ctx) {
        if(!(ctx.term().getText().equals("")))
            visitTerm(ctx.term());

        if(!(ctx.t().getText().equals("")))
            visitT(ctx.t());

        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar();

        String op2 = operandos.pop();

        if(ctx.SUMA() != null) {
            output += operandos.pop() + " + " + op2;
        }
        else if(ctx.RESTA() != null) {
            output += operandos.pop() + " - " + op2;
        }

        operandos.push(gv.getVar());

        return output;
    }

    @Override
    public String visitTerm(TermContext ctx) {
        if(!(ctx.factor().getText().equals("")))
            visitFactor(ctx.factor());

        if(!(ctx.f().getText().equals(""))) 
            visitF(ctx.f());

        return output;
    }

    @Override
    public String visitF(FContext ctx) {
        GeneradorVars gv = GeneradorVars.getInstance();
        output += gv.getNewVar() + operandos.pop();

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

        operandos.push(gv.getVar());

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
            output += "op";
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

    public void writeFile() { 
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