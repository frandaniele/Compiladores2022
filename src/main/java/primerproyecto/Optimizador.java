package primerproyecto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Optimizador {

    public static void main( String[] args ) {
        Set<String> objectives = new HashSet<String>();
        LinkedList<String> blocks = new LinkedList<String>();
        List<String> code = null;

        try {
			code = Files.readAllLines(Paths.get("tac"));           
		} catch (IOException e) {
			e.printStackTrace();
		}

        while(code.get(0).equals(""))
            code.remove(0);

        for (String line : code) {
            if(line.startsWith("\tifz ") || line.startsWith("\tjmp l"))
                objectives.add("lbl " + line.substring(line.indexOf('l')));
        }
        
        String block = "";
        for (String line : code) {
            if(objectives.contains(line) && !block.equals("")) {
                blocks.add(block);
                block = "";
            }

            if(!(line.equals("")))
                block += line + "\n";

            if(line.startsWith("\tifz ") || line.startsWith("\tjmp l") || line.startsWith("\tret")) {
                blocks.add(block);
                block = "";
            }            
        }
    }
}
