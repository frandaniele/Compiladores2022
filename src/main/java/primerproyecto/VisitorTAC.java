package primerproyecto;

import primerproyecto.tacParser.AsignacionContext;
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


    public VisitorTAC() {

    }

    @Override
    public String visitAsignacion(AsignacionContext ctx) {
        return output;
    }

    @Override
    public String visitBloque_basico(Bloque_basicoContext ctx) {
        return output;
    }

    @Override
    public String visitBloques_basicos(Bloques_basicosContext ctx) {
        return output;
    }

    @Override
    public String visitCtrl_instr(Ctrl_instrContext ctx) {
        return output;
    }

    @Override
    public String visitIf_tac(If_tacContext ctx) {
        return output;
    }

    @Override
    public String visitInstruccion(InstruccionContext ctx) {
        return output;
    }

    @Override
    public String visitInstrucciones(InstruccionesContext ctx) {
        return output;
    }

    @Override
    public String visitJump(JumpContext ctx) {
        return output;
    }

    @Override
    public String visitLabel(LabelContext ctx) {
        return output;
    }

    @Override
    public String visitOperacion(OperacionContext ctx) {
        return output;
    }

    @Override
    public String visitPop(PopContext ctx) {
        return output;
    }

    @Override
    public String visitPrograma(ProgramaContext ctx) {
        return super.visitPrograma(ctx);
    }

    @Override
    public String visitPush(PushContext ctx) {
        return output;
    }
}