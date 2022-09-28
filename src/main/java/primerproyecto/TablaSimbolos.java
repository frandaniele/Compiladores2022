package primerproyecto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public final class TablaSimbolos {
    private LinkedList< Map<String, Id> > lista;
    private static TablaSimbolos instance;

    public TablaSimbolos() {
        this.lista = new LinkedList< Map<String, Id> >();
    }
    
    public static TablaSimbolos getInstance() {
        if(instance == null)
            instance = new TablaSimbolos();

        return instance;
    }

    public LinkedList<Map<String, Id>> getLista() {
        return lista;
    }

    public void setLista(LinkedList<Map<String, Id>> lista) {
        this.lista = lista;
    }

    public void addContext() {
        lista.add(new HashMap<String, Id>());
    }

    public void delContext() {
        lista.removeLast();
    }
    
    public void addSimbolo(Id id) {
        lista.getLast().put(id.getNombre(), id); //putIfAbsent?
    }

    public Boolean buscarSimbolo(Id id) {
        for(Map<String, Id> context : lista) {
            if(context.containsKey(id.getNombre()))
                return true;
        }

        return false;
    }

    public Boolean buscarSimboloLocal(Id id) {
        return lista.getLast().containsKey(id.getNombre());
    }
}