package primerproyecto;

import java.util.LinkedList;

public class Funcion extends Id {
    private LinkedList<TipoDato> args;

    public Funcion(String nombre, TipoDato tipo, Boolean usado, Boolean init) {
        super.nombre = nombre;
        super.tipo = tipo;
        super.usado = usado;
        super.init = init;
        this.args = new LinkedList<TipoDato>();
    }

    public LinkedList<TipoDato> getArgs() {
        return args;
    }

    public void setArgs(LinkedList<TipoDato> args) {
        this.args = args;
    }    
    
    public void addArg(TipoDato td) {
        System.out.println("FUncion: agrego param " + td.toString());
        args.add(td);
    }
}
