package primerproyecto;

import java.util.LinkedList;

public class Funcion extends Id {
    private LinkedList<TipoDato> args;
    private Integer estado;// 0 no existe, 1 prototipada, 2 inicializada

    public Funcion(String nombre, TipoDato tipo, Boolean usado, Boolean init) {
        super.nombre = nombre;
        super.tipo = tipo;
        super.usado = usado;
        super.init = init;
        this.args = new LinkedList<TipoDato>();
        this.estado = 0;
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

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }
}
