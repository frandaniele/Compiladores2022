package primerproyecto;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class FileRW {
    public LinkedList<HashMap<String, Integer>> readFile(String path) {
        LinkedList<HashMap<String, Integer>> simbolos = new LinkedList<HashMap<String, Integer>>();

		try {
			List<String> allLines = Files.readAllLines(Paths.get(path));
			for (String line : allLines) {
                simbolos.add(new HashMap<String, Integer>());
                Scanner scan = new Scanner(line.replace("[", "").replace("]", "")).useDelimiter(",");
                while(scan.hasNext()) {
                    simbolos.getLast().put(scan.next().trim(), 0);
                }
                scan.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

        return simbolos;
	}

    public void writeFile(FileWriter fichero, String output) { 
        PrintWriter pw = null;
        
        try {
            pw = new PrintWriter(fichero);

            pw.println(output);
        } 
        catch (Exception e) {
            e.printStackTrace();
        } 
    }
}