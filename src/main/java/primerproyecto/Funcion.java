package primerproyecto;

import java.util.LinkedList;

public class Funcion extends Id {
    private LinkedList<TipoDato> args;
    private Boolean prototipo;

    public Funcion(String nombre, TipoDato tipo, Boolean usado, Boolean init, Boolean prototipo) {
        super.nombre = nombre;
        super.tipo = tipo;
        super.usado = usado;
        super.init = init;
        this.prototipo = prototipo;
        this.args = new LinkedList<TipoDato>();
    }

    public LinkedList<TipoDato> getArgs() {
        return args;
    }

    public void setArgs(LinkedList<TipoDato> args) {
        this.args = args;
    }    
    
    public void addArg(TipoDato td) {
        args.add(td);
    }

    public Boolean getPrototipo() {
        return prototipo;
    }

    public void setPrototipo(Boolean prototipo) {
        this.prototipo = prototipo;
    }
}
