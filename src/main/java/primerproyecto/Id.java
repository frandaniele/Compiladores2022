package primerproyecto;

public abstract class Id {
    protected String nombre;
    protected TipoDato tipo;
    protected Boolean usado;
    protected Boolean init;
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
    
    public void setUsado(Boolean usado) {
        this.usado = usado;
    }

    public void setInit(Boolean init) {
        this.init = init;
    }

    public Boolean getUsado() {
        return usado;
    }
    
    public Boolean getInit() {
        return init;
    }
    
    public void setTipo(TipoDato tipo) {
        this.tipo = tipo;
    }
    
    public TipoDato getTipo() {
        return tipo;
    }
}