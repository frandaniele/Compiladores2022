package primerproyecto;

public class Variable extends Id{

    public Variable(String nombre, TipoDato tipo, Boolean usado, Boolean init) {
        super.nombre = nombre;
        super.tipo = tipo;
        super.usado = usado;
        super.init = init;
    }
}