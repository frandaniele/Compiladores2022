package primerproyecto;

import java.util.LinkedList;

public class Generador {
    private static Generador instance;
    private Integer numV, numL;
    private LinkedList<String> vars, labels;

    public Generador() {
        this.numV = 0;
        this.numL = 0;
        this.vars = new LinkedList<String>();
        this.labels = new LinkedList<String>();
    }
    
    public static Generador getInstance() {
        if(instance == null) 
            instance = new Generador();

        return instance;
    }
    
    public Integer getnumV() {
        return numV;
    }

    public LinkedList<String> getVars() {
        return vars;
    }

    public Integer getnumL() {
        return numL;
    }

    public LinkedList<String> getLabels() {
        return labels;
    }

    public String getVar() {
        return vars.pop();
    }

    public String getLabel() {
        return labels.pop();
    }

    public String getNewVar() {
        vars.add("t" + numV);

        return "\nt" + numV++ + " = ";
    }

    public String getNewLabel() {
        vars.add("l" + numL);

        return "\nlbl l" + numL++;
    }

    public void resetVars() {
        this.numV = 0;
    }

    public void resetLabels() {
        this.numL = 0;
    }
}
