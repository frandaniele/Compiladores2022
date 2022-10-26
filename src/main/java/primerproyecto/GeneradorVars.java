package primerproyecto;

import java.util.LinkedList;

public class GeneradorVars {
    private static GeneradorVars instance;
    private Integer num;
    private LinkedList<String> vars;

    public GeneradorVars() {
        this.num = 0;
        this.vars = new LinkedList<String>();
    }
    
    public static GeneradorVars getInstance() {
        if(instance == null) 
            instance = new GeneradorVars();

        return instance;
    }
    
    public Integer getNum() {
        return num;
    }

    public String getVar() {
        return vars.pop();
    }

    public String getNewVar() {
        vars.add("t" + num);

        return "\nt" + num++ + " = ";
    }

    public void reset() {
        this.num = 0;
    }

    public LinkedList<String> getVars() {
        return vars;
    }
}
