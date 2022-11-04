package primerproyecto;

import java.util.HashMap;
import java.util.LinkedList;

import primerproyecto.tacParser.Bloque_basicoContext;
import primerproyecto.tacParser.Bloques_basicosContext;
import primerproyecto.tacParser.Ctrl_instrContext;
import primerproyecto.tacParser.If_tacContext;
import primerproyecto.tacParser.InstruccionContext;
import primerproyecto.tacParser.InstruccionesContext;
import primerproyecto.tacParser.JumpContext;
import primerproyecto.tacParser.LabelContext;
import primerproyecto.tacParser.OperacionContext;
import primerproyecto.tacParser.PopContext;
import primerproyecto.tacParser.ProgramaContext;
import primerproyecto.tacParser.PushContext;

public class VisitorTAC extends tacBaseVisitor<String>{
    private String output = "";
    private HashMap<String, Integer> vars;
    private LinkedList<String> used;
    private Integer num = 1;

    public VisitorTAC() {
        vars = new HashMap<String, Integer>();
        used = new LinkedList<String>();
    }

    @Override
    public String visitPrograma(ProgramaContext ctx) {
        output = "";
        visitChildren(ctx);
            
        FileRW fileHandler = new FileRW();
        fileHandler.writeFile("tac_optimizado" + num, output);
        num++;

        return output;
    }

    @Override
    public String visitBloques_basicos(Bloques_basicosContext ctx) {
        visitChildren(ctx);
        return output;
    }

    @Override
    public String visitBloque_basico(Bloque_basicoContext ctx) {
        if(!(ctx.instrucciones().getText().equals(""))) 
            visitInstrucciones(ctx.instrucciones());
        else 
            System.out.println("este bloque se salto");
            
        visitCtrl_instr(ctx.ctrl_instr());
            
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
    public String visitOperacion(OperacionContext ctx) {
        String variableAsignada;
        Integer value = 0;

        variableAsignada = ctx.asignacion().getChild(0).getText();

        if(ctx.OPERADOR() != null) {
            String op1 = "0", op2 = "0";
            Boolean opero = true;

            if(vars.containsKey(ctx.getChild(1).getText()))
                op1 = String.valueOf(vars.get(ctx.getChild(1).getText()));
            else {
                op1 = ctx.getChild(1).getText();
                if(ctx.ENTERO(0) == null) 
                    opero = false;
            }

            if(vars.containsKey(ctx.getChild(3).getText()))
                op2 = String.valueOf(vars.get(ctx.getChild(3).getText()));
            else {
                op2 = ctx.getChild(3).getText();
                if(ctx.ENTERO(1) == null) 
                    opero = false;
            }

            if(opero) {
                value = operate(ctx.OPERADOR().getText(), op1, op2);
                vars.put(variableAsignada, value);
                output += "\n\t" + variableAsignada + " = " + value;
            }
            else 
                output += "\n\t" + ctx.getText();
        }
        else {
            if(vars.containsKey(ctx.getChild(1).getText())) {
                value = vars.get(ctx.getChild(1).getText());
            }
            else {
                if(ctx.ENTERO() == null) {//es tmp o id
                    used.add(ctx.getChild(1).getText());
                    output += "\n\t" + variableAsignada + " = " + ctx.getChild(1).getText();
                    return output;
                }
                value = Integer.parseInt(ctx.ENTERO(0).getText());
            }
                    
            vars.put(variableAsignada, value);
            output += "\n\t" + variableAsignada + " = " + value;
        }

        return output;
    }

    @Override
    public String visitCtrl_instr(Ctrl_instrContext ctx) {
        if(ctx.RET() != null) {
            vars.clear(); //estoy en otra funcion
            output += "\n\tret\n";
        }
        else
            visitChildren(ctx);
        
        return output;
    }

    @Override
    public String visitIf_tac(If_tacContext ctx) {
        Integer val = 0;
        String var;

        if(vars.containsKey(ctx.getChild(1).getText())) {
            val = vars.get(ctx.getChild(1).getText());
            var = String.valueOf(val);
        }
        else {
            var = ctx.getChild(1).getText();
            if(ctx.ENTERO() == null)//es tmp o id
                used.add(ctx.getChild(1).getText());
        }
        
        output += "\n\t" + ctx.IFZ().getText() + " " + var + " " + ctx.GOTO().getText() + " " + ctx.ETIQ().getText();

        return output;
    }

    @Override
    public String visitJump(JumpContext ctx) {
        output += "\n\t" + ctx.JMP().getText() + " " + ctx.ETIQ().getText();

        return output;
    }

    @Override
    public String visitLabel(LabelContext ctx) {
        output += "\n" + ctx.LBL().getText() + " " + ctx.ETIQ().getText();

        return output;
    }

    @Override
    public String visitPop(PopContext ctx) {
        output += "\n\t" + ctx.POP().getText() + " " + ctx.getChild(1).getText();

        return output;
    }

    @Override
    public String visitPush(PushContext ctx) {
        output += "\n\t" + ctx.PUSH().getText() + " " + ctx.getChild(1).getText();

        return output;
    }

    private Integer operate(String operador, String op1, String op2) {
        Integer a = Integer.parseInt(op1), b = Integer.parseInt(op2);

        switch (operador) {
            case "+":
                return a + b;
        
            case "-":
                return a - b;

            case "*":
                return a * b;
        
            case "/":
                return a / b;

            case "%":
                return a % b;
        
            case "|":
                return a | b;

            case "&":
                return a & b;
            
            case "||":
                return boolToInt(intToBool(a) || intToBool(b));

            case "&&":
                return boolToInt(intToBool(a) && intToBool(b));
        
            case "<":
                return boolToInt(a < b);

            case ">":
                return boolToInt(a > b);
        
            case ">=":
                return boolToInt(a >= b);

            case "<=":
                return boolToInt(a <= b);
        
            case "==":
                return boolToInt(a == b);
        
            default:
                return 0;
        }
    }

    private Integer boolToInt(Boolean b) {
        return (b) ? 1 : 0;
    }

    private Boolean intToBool(Integer i) {
        if(i == 0)
            return false;

        return true;
    }
}