package primerproyecto;

public class GeneradorLabels {
    private static GeneradorLabels instance;
    private Integer num;

    public GeneradorLabels() {
        this.num = 0;
    }
    
    public static GeneradorLabels getInstance() {
        if(instance == null) 
            instance = new GeneradorLabels();

        return instance;
    }

    public String getLabel() {
        return "l" + num++;
    }

    public void reset() {
        this.num = 0;
    }
}
