package primerproyecto;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.antlr.v4.runtime.tree.ErrorNode;

import primerproyecto.declaracionesParser.AsignacionContext;
import primerproyecto.declaracionesParser.DeclaracionContext;
import primerproyecto.declaracionesParser.InstruccionContext;
import primerproyecto.declaracionesParser.InstruccionesContext;
import primerproyecto.declaracionesParser.ProgramaContext;

public class Visitor extends declaracionesBaseVisitor<Object> {
    private String texto = "";
    private List<ErrorNode> errores;
    private static LinkedList<List<String>> simbolos;
    
    public Visitor() {
        errores = new ArrayList<ErrorNode>();
        simbolos = new LinkedList<List<String>>();
        readFile();
    }
        
    @Override
    public String visitPrograma(ProgramaContext ctx) {
        visitChildren(ctx);

        return texto;
    }

    @Override
    public String visitInstrucciones(InstruccionesContext ctx) {
        visitChildren(ctx.getRuleContext());

        return texto;
    }
    
    @Override
    public String visitInstruccion(InstruccionContext ctx) {
        visitChildren(ctx);

        return texto;
    }
    
    @Override
    public String visitDeclaracion(DeclaracionContext ctx) {
        visitChildren(ctx);

        return texto;
    }

    @Override
    public Object visitAsignacion(AsignacionContext ctx) {
        System.out.println(ctx.ID().getText() + " = " + ctx.oal().getText());
        return texto;
    }

    @Override
    public String visitErrorNode(ErrorNode node) {
        addErrorNode(node);
        
        return texto;
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
                simbolos.add(new LinkedList<String>());
                Scanner scan = new Scanner(line.replace("[", "").replace("]", "")).useDelimiter(",");
                while(scan.hasNext()) {
                    simbolos.getLast().add(scan.next().trim());
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

            pw.println("Codigo de 3 direcciones\n-----------------------\n");
            pw.println(texto);
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