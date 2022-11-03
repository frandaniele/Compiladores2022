package primerproyecto;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public final class TablaSimbolos {
    private LinkedList< Map<String, Id> > lista;
    private static TablaSimbolos instance;
    private String output = "";

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
        LinkedList<String> l = new LinkedList<String>();

        for(Id id : lista.getLast().values()){ 
            if(id.getUsado() == false && !(id.getNombre().equals("main"))) {
                System.out.println("variable " + id.getNombre() + " not used");
            }
            else if(id.getInit() == false) {
                System.out.println("variable " + id.getNombre() + " never defined");
            }
            else
                l.add(id.getNombre());
        }
        
        output += l.toString() + "\n";

        lista.removeLast();

        if(lista.isEmpty()) {
            FileWriter fichero = null;

            try {
                fichero = new FileWriter("simbolos");
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileRW fileHandler = new FileRW();
            fileHandler.writeFile(fichero, output);

            try {
                fichero.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public void addSimbolo(Id id) {
        lista.getLast().put(id.getNombre(), id); //putIfAbsent?
    }

    public Id buscarSimbolo(String id) {
        Iterator<Map<String, Id>> x = lista.descendingIterator(); 
  
        while(x.hasNext()) { 
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
}