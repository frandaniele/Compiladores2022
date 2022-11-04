package primerproyecto;

import java.util.HashMap;

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
    private Boolean skip_block = false;

    public VisitorTAC() {
        vars = new HashMap<String, Integer>();
    }

    @Override
    public String visitPrograma(ProgramaContext ctx) {
        visitChildren(ctx);
            
        FileRW fileHandler = new FileRW();
        fileHandler.writeFile("tac_optimizado", output);

        return output;
    }

    @Override
    public String visitBloques_basicos(Bloques_basicosContext ctx) {
        visitChildren(ctx);
        return output;
    }

    @Override
    public String visitBloque_basico(Bloque_basicoContext ctx) {
        if(!(ctx.instrucciones().getText().equals("")) && !skip_block) 
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

        if(ctx.asignacion().ID() != null) 
            variableAsignada = ctx.asignacion().ID().getText();
        else 
            variableAsignada = ctx.asignacion().TMP().getText();


        if(ctx.OPERADOR() != null) {
            String op1 = "0", op2 = "0";
            Boolean opero = true;

            if(ctx.ENTERO(0) != null) 
                op1 = ctx.ENTERO(0).getText();
            else if(ctx.TMP(0) != null) {
                if(vars.containsKey(ctx.TMP(0).getText()))
                    op1 = String.valueOf(vars.get(ctx.TMP(0).getText()));
                else {
                    opero = false;
                    op1 = ctx.TMP(0).getText();
                }
            }
            else if(ctx.ID(0) != null) {
                if(vars.containsKey(ctx.ID(0).getText())) 
                    op1 = String.valueOf(vars.get(ctx.ID(0).getText()));
                else {
                    opero = false;
                    op1 = ctx.ID(0).getText();
                }
            }
            
            if(ctx.ENTERO(1) != null)
                op2 = ctx.ENTERO(1).getText();
            else if(ctx.TMP(1) != null) {
                if(vars.containsKey(ctx.TMP(1).getText()))
                    op2 = String.valueOf(vars.get(ctx.TMP(1).getText()));
                else {
                    opero = false;
                    op2 = ctx.TMP(1).getText();
                }
            }
            else if(ctx.ID(1) != null) {
                if(vars.containsKey(ctx.ID(1).getText())) 
                    op2 = String.valueOf(vars.get(ctx.ID(1).getText()));
                else {
                    opero = false;
                    op2 = ctx.ID(1).getText();
                }
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
            if(ctx.ID(0) != null) {
                if(vars.containsKey(ctx.ID(0).getText()))
                    value = vars.get(ctx.ID(0).getText());
                else {
                    output += "\n\t" + variableAsignada + " = " + ctx.ID(0).getText();
                    return output;
                }

            }
            else if(ctx.TMP(0) != null) {
                if(vars.containsKey(ctx.TMP(0).getText()))
                    value = vars.get(ctx.TMP(0).getText());
                else {
                    output += "\n\t" + variableAsignada + " = " + ctx.TMP(0).getText();
                    return output;
                }
            }
            else
                value = Integer.parseInt(ctx.ENTERO(0).getText());
                
            vars.put(variableAsignada, value);
            output += "\n\t" + variableAsignada + " = " + value;
        }

        return output;
    }

    @Override
    public String visitCtrl_instr(Ctrl_instrContext ctx) {
        if(skip_block)
            skip_block = false;
        else if(ctx.RET() != null)
            output += "\n\tret\n";
        else
            visitChildren(ctx);
        
        return output;
    }

    @Override
    public String visitIf_tac(If_tacContext ctx) {
        if(ctx.TMP() != null) 
            if(vars.containsKey(ctx.TMP().getText()))
                if(vars.get(ctx.TMP().getText()) == 0) 
                    skip_block = true;

        else if(ctx.ID() != null) 
            if(vars.containsKey(ctx.ID().getText()))
                if(vars.get(ctx.ID().getText()) == 0) 
                    skip_block = true;

        else if(ctx.ENTERO() != null) 
            if(ctx.ENTERO().getText().equals("0"))
                skip_block = true;

        if(!skip_block)
            output += "\n\t" + ctx.IFZ().getText() + " " + ctx.getChild(1).getText() + " " + ctx.GOTO().getText() + " " + ctx.ETIQ().getText();

        return output;
    }

    @Override
    public String visitJump(JumpContext ctx) {
        output += "\n\t" + ctx.JMP().getText() + " " + ctx.ETIQ().getText();

        return output;
    }

    @Override
    public String visitLabel(LabelContext ctx) {
        output += "\n" + ctx.ETIQ().getText();
        
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