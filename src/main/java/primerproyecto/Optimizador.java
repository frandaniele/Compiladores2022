package primerproyecto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Optimizador {
    private LinkedList<LinkedList<String>> blocks;
    private List<String> code;
    private String output;

    public Optimizador() {
        blocks = new LinkedList<LinkedList<String>>();
        output = "";
        code = null;        
    }
     
    public void Optimizar(String path) {
        code = getCode(path);

        blocks = getBlocks(code);

        output = getOutput(blocks);

        FileRW file_handler = new FileRW();
        file_handler.writeFile("tac_optimizado", output);
    }

    /**
     * A partir del path de un archivo de codigo de tres direcciones
     * lo lee y devuelve una lista con cada linea de codigo
     * 
     * @param path el path del archivo a optimizar
     * @return una lista con las lineas de codigo
     */
    private List<String> getCode(String path) {
        List<String> code = null;

        try {
			code = Files.readAllLines(Paths.get(path));           
		} catch (IOException e) {
			e.printStackTrace();
		}

        while(code.get(0).equals(""))
            code.remove(0);

        return code;
    }

    /**
     * Esta funcion toma una lista de strings que son el codigo del programa a optimizar
     * y divide el codigo en bloques basicos de TAC
     * 
     * @param code las lineas de codigo
     * @return una lista de bloques basicos (bloque basico -> lista de lineas de codigo)
     */
    private LinkedList<LinkedList<String>> getBlocks(List<String> code) {
        LinkedList<LinkedList<String>> blocks = new LinkedList<LinkedList<String>>();
        HashSet<String> objectives = new HashSet<String>();
        
        for (String line : code) {
            if(line.startsWith("\tifz ") || line.startsWith("\tjmp l"))
                objectives.add("lbl " + line.substring(line.indexOf('l')));
        }
        
        LinkedList<String> block = new LinkedList<String>();
        for (String line : code) {
            if(objectives.contains(line) && !block.isEmpty()) {
                blocks.add(block);
                block = new LinkedList<String>();
            }

            if(!(line.equals("")))
                block.add(line);

            if(line.startsWith("\tifz ") || line.startsWith("\tjmp l") || line.startsWith("\tret")) {
                blocks.add(block);
                block = new LinkedList<String>();
            }            
        }

        return blocks;
    }

    /**
     * Analiza cada bloque basico del TAC y genera un output del codigo optimizado
     * 
     * @param blocks 
     * @return string con el codigo optimizado
     */
    private String getOutput(LinkedList<LinkedList<String>> blocks) {
        LinkedList<String> used = new LinkedList<String>();
        String output = "";

        for(LinkedList<String> b : blocks){
            HashMap<String, Integer> vars = new HashMap<String, Integer>();

            for(String l : b) {
                if(l.contains("=")) { //es una operacion
                    String[] operacion = l.split("=",2);
                    String variableAsignada = operacion[0].trim();
                    String[] ops = operacion[1].split("[|][|]|[&][&]|[><=][=]|[-+/%|&<>*]");
                    String op1 = ops[0].trim();
                    String operator = getOperator(l);

                    Integer value = 0;

                    if(operator != null) {//var = x op y
                        String op2 = ops[1].trim();
                        Boolean opero = true;

                        if(vars.containsKey(op1))//es una var que conozco su valor
                            op1 = String.valueOf(vars.get(op1));
                        else {//es un num o var que no conozco lo que vale
                            if(isTmpOrId(op1.charAt(0))) {//var que no conozco valor
                                used.add(op1);
                                opero = false;
                            }
                        }

                        if(vars.containsKey(op2))//es una var que conozco su valor
                            op2 = String.valueOf(vars.get(op2));
                        else {//es un num o var que no conozco lo que vale
                            if(isTmpOrId(op2.charAt(0))) {//var que no conozco valor
                                used.add(op2);
                                opero = false;
                            }
                        }

                        if(opero) {//tengo disponibles los valores
                            value = operate(operator, op1, op2);
                            vars.put(variableAsignada, value);
                            output += "\n\t" + variableAsignada + " = " + value;
                        }
                        else //no pude obtener un resultado
                            output += "\n" + l;
                    }
                    else {//var = x
                        if(vars.containsKey(op1)) {//es var que conozco el valor
                            value = vars.get(op1);
                            vars.put(variableAsignada, value);
                            output += "\n\t" + variableAsignada + " = " + value;
                        }
                        else {
                            if(isTmpOrId(op1.charAt(0))) {//es tmp o id q no conozco valor
                                used.add(op1);
                                output += "\n" + l;
                            }
                            else {//es un numero
                                value = (int) Double.parseDouble(op1);
                                vars.put(variableAsignada, value);
                                output += "\n\t" + variableAsignada + " = " + value;
                            }                                   
                        }
                    }
                }
                else if(l.contains("push")) {
                    String var = l.substring(l.trim().indexOf(" ") + 1).trim();
                    if(isTmpOrId(var.charAt(0)))
                        used.add(var);

                    output += "\n" + l;
                }
                else if(l.contains("pop")) {
                    output += "\n" + l;
                }
                else if(l.contains("ret")) {
                    output += "\n" + l;
                }
                else if(l.contains("ifz")) {
                    Integer val = 0;
                    String var = l.substring(4, l.indexOf("goto")).trim();
                   
                    if(vars.containsKey(var)) {//si existe el valor reemplazo la variable
                        val = vars.get(var);
                        var = String.valueOf(val);
                        
                        String label = l.substring(l.indexOf("goto") + 5).trim();
                        output += "\n\tifz " + var + " goto " + label;
                    }
                    else {//es un num o no conozco el valor
                        if(isTmpOrId(var.charAt(0)))//es tmp o id
                            used.add(var);
                        
                        output += "\n" + l;
                    }

                }
                else if(l.contains("lbl")) {
                    output += "\n" + l;
                }
                else if(l.contains("jmp")) {
                    output += "\n" + l;
                }
            }
        }

        return output;
    }

    private static Integer operate(String operador, String op1, String op2) {
        Integer a = (int)Double.parseDouble(op1), b = (int)Double.parseDouble(op2);

        switch (operador) {
            case "+":
                return a + b;
        
            case "-":
                return a - b;

            case "*":
                return a * b;
        
            case "/":
                return a / b;

            case "%":
                return a % b;
        
            case "|":
                return a | b;

            case "&":
                return a & b;
            
            case "||":
                return boolToInt(intToBool(a) || intToBool(b));

            case "&&":
                return boolToInt(intToBool(a) && intToBool(b));
        
            case "<":
                return boolToInt(a < b);

            case ">":
                return boolToInt(a > b);
        
            case ">=":
                return boolToInt(a >= b);

            case "<=":
                return boolToInt(a <= b);
        
            case "==":
                return boolToInt(a == b);
        
            default:
                return 0;
        }
    }

    private static Integer boolToInt(Boolean b) {
        return (b) ? 1 : 0;
    }

    private static Boolean intToBool(Integer i) {
        if(i == 0)
            return false;

        return true;
    }

    private static Boolean isTmpOrId(Character c) {
        return Character.isAlphabetic(c) || c == '_';
    }

    private static String getOperator(String operacion) {
        if(operacion.contains("+"))
            return "+";
        else if(operacion.contains("-"))
            return "-";
        else if(operacion.contains("*"))
            return "*";
        else if(operacion.contains("/"))
            return "/";
        else if(operacion.contains("%"))
            return "%";
        else if(operacion.contains("|"))
            return "|";
        else if(operacion.contains("&"))
            return "&";
        else if(operacion.contains("<"))
            return "<";
        else if(operacion.contains(">"))
            return ">";
        else if(operacion.contains(">="))
            return ">=";
        else if(operacion.contains("<="))
            return "<=";
        else if(operacion.contains("=="))
            return "==";
        else if(operacion.contains("||"))
            return "||";
        else if(operacion.contains("&&"))
            return "&&";

        return null;
    }
}
