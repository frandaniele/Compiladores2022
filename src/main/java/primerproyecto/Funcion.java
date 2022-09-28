package primerproyecto;

import java.util.List;

public class Funcion extends Id {
    private List<TipoDato> args;

    public Funcion(String nombre, TipoDato tipo, Boolean usado, Boolean init, List<TipoDato> args) {
        super.nombre = nombre;
        super.tipo = tipo;
        super.usado = usado;
        super.init = init;
        this.args = args;
    }

    public List<TipoDato> getArgs() {
        return args;
    }

    public void setArgs(List<TipoDato> args) {
        this.args = args;
    }    
    
    public void addArg(TipoDato td) {
        args.add(td);
    }
}
