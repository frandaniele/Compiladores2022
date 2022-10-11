package primerproyecto;

import org.antlr.v4.runtime.tree.TerminalNode;

import primerproyecto.declaracionesParser.BloqueContext;
import primerproyecto.declaracionesParser.ProgramaContext;

public class Visitor extends declaracionesBaseVisitor<Object> {

    @Override
    public Object visitBloque(BloqueContext ctx) {
        System.out.println(" == Bloque tiene " + ctx.getChildCount() + " hijos");
        
        System.out.println(" == hijo 0 -> " + ctx.getChild(0).getText());
        System.out.println(" == hijo 1 -> " + ctx.getChild(1).getText());
        System.out.println(" == hijo 2 -> " + ctx.getChild(2).getText());
        
        return super.visitBloque(ctx);
    }

    @Override
    public Object visitPrograma(ProgramaContext ctx) {
        System.out.println("comienzo visita arbol");
        
        Object o = super.visitPrograma(ctx);

        System.out.println("fin de la visita");
        
        return o;
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
        System.out.println("\tHoja contiene |" + node.getText() + "|");
        return super.visitTerminal(node);
    }
    
}
