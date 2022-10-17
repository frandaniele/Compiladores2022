package primerproyecto;

import java.util.HashMap;
import java.util.Iterator;
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
        for(Id id : lista.getLast().values()){ 
            if(id.getUsado() == false) {
                System.out.println("variable " + id.getNombre() + " not used");
            }

            if(id.getInit() == false) {
                System.out.println("variable " + id.getNombre() + " never defined");
            }
        }

        lista.removeLast();
    }
    
    public void addSimbolo(Id id) {
        lista.getLast().put(id.getNombre(), id); //putIfAbsent?
    }

    public Id buscarSimbolo(String id) {//probablemente sea mas eficiente buscar desde el ultimo contexto
        Iterator<Map<String, Id>> x = lista.descendingIterator(); 
  
        while (x.hasNext()) { 
            Map<String, Id> context = x.next();
            if(context.containsKey(id))
                return context.get(id);
        } 
        
        return null;
    }

    public Id buscarSimboloLocal(String id) {
        if(lista.getLast().containsKey(id))
            return lista.getLast().get(id);
        
        return null;
    }

    private void printTabla() {
        int c = 0;
        System.out.println("\n-------------TABLA-------------------\nContextos antes de eliminar el ultimo");
        for(Map<String, Id> context : lista) {
            System.out.println("tabla de simbolos " + c + ": " + context.keySet());
            c++;
        }
        System.out.println("-------------------------------------\n");
    }
}