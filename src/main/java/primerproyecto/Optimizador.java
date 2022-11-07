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
    private LinkedList<LinkedList<String>> used;
    private List<String> code;
    private String output;
    private Integer pasada;

    public Optimizador() {
        blocks = new LinkedList<LinkedList<String>>();
        used = new LinkedList<LinkedList<String>>();
        output = "";
        code = null;    
        pasada = 1;    
    }
     
    public String Optimizar(String path) {
        code = getCode(path);

        blocks = getBlocks(code);

        output = getOutput(blocks);

        FileRW file_handler = new FileRW();
        file_handler.writeFile("tac_optimizado" + pasada, output);

        return "tac_optimizado" + pasada++;
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
        
        for (String line : code) {//los objetivos de un salto son lideres de bloques
            if(line.startsWith("\tifz ") || line.startsWith("\tjmp l"))
                objectives.add("lbl " + line.substring(line.indexOf('l')));
        }
        
        LinkedList<String> block = new LinkedList<String>();
        for (String line : code) {
            if(objectives.contains(line) && !block.isEmpty()) {//si estoy por arrancar un nuevo bloque agrego y reinicio
                blocks.add(block);
                block = new LinkedList<String>();
            }

            if(!(line.equals("")))
                block.add(line);

            if(line.startsWith("\tifz ") || line.startsWith("\tjmp l") || line.startsWith("\tret")) {
                blocks.add(block);//si estoy por arrancar un nuevo bloque agrego y reinicio
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
        String output = "";

        used.add(new LinkedList<String>());//las usadas en cada pasada

        for(LinkedList<String> b : blocks){
            HashMap<String, Integer> vars = new HashMap<String, Integer>();
            HashMap<String, String> asignaciones = new HashMap<String, String>();
            Boolean noElimine = true;
            
            for(String l : b) {
                if(l.contains("=")) { //es una operacion
                    String[] operacion = l.split("=",2);
                    String variableAsignada = operacion[0].trim();

                    if(pasada > 1 && !used.get(pasada - 2).contains(variableAsignada))//elimino no usados
                        continue;                    

                    Integer value = 0;
                    String[] ops = operacion[1].split("[|][|]|[&][&]|[><=][=]|[-+/%|&<>*]");
                    String op1 = ops[0].trim();
                    String operator = getOperator(l);

                    if(op1.length() == 0) {//caso negativos
                        op1 = ops[1].trim();

                        if(vars.containsKey(op1)) {//es var que conozco el valor
                            value = -vars.get(op1);
                            vars.put(variableAsignada, value);
                            asignaciones.put(variableAsignada, "\n\t" + variableAsignada + " = " + value);
                        }
                        else {
                            if(isTmpOrId(op1.charAt(0))) {//es tmp o id q no conozco valor
                                used.getLast().add(op1);
                                output += "\n\t" + variableAsignada + " = -" + op1;
                            }
                            else {//es un numero
                                value = -(int) Double.parseDouble(op1);
                                vars.put(variableAsignada, value);
                                asignaciones.put(variableAsignada, "\n\t" + variableAsignada + " = " + value);
                            }                                   
                        }
                    }
                    else if(operator != null) {//var = x op y
                        String op2 = ops[1].trim();                       
                        Boolean opero = true;

                        if(vars.containsKey(op1))//es una var que conozco su valor
                            op1 = String.valueOf(vars.get(op1));
                        else {//es un num o var que no conozco lo que vale
                            if(isTmpOrId(op1.charAt(0))) {//var que no conozco valor
                                if(asignaciones.containsKey(op1)) {//escribo la ultima asignacion conocida
                                    String asignacion = asignaciones.get(op1);
                                    output += asignacion;
                                    
                                    String aux = op1;
                                    if(getOperator(asignacion) == null) //x = y
                                        op1 = asignacion.substring(asignacion.indexOf("=") + 1).trim();
                                    
                                    asignaciones.remove(aux);
                                }

                                used.getLast().add(op1);
                                opero = false;
                            }
                        }
                        
                        if(vars.containsKey(op2))//es una var que conozco su valor
                            op2 = String.valueOf(vars.get(op2));
                        else {//es un num o var que no conozco lo que vale
                            if(isTmpOrId(op2.charAt(0))) {//var que no conozco valor
                                if(asignaciones.containsKey(op2)) {//escribo la ultima asignacion conocida
                                    String asignacion = asignaciones.get(op2);
                                    output += asignacion;
                                    
                                    String aux = op2;
                                    if(getOperator(asignacion) == null) //x = y
                                        op2 = asignacion.substring(asignacion.indexOf("=") + 1).trim();
                                    
                                    asignaciones.remove(aux);
                                }

                                used.getLast().add(op2);
                                opero = false;
                            }
                        }

                        if(opero) {//tengo disponibles los valores
                            value = operate(operator, op1, op2);
                            vars.put(variableAsignada, value);
                            asignaciones.put(variableAsignada,"\n\t" + variableAsignada + " = " + value);
                        }
                        else {//no pude obtener un resultado
                            if(operacionNeutra(operator, op1, op2)) {//resuelvo 1*x, 0+x etc
                                String op = op2;

                                if(op2.equals("1") && (operator.equals("*") || operator.equals("/")))
                                    op = op1;
                                else if(op2.equals("0") && (operator.equals("+") || operator.equals("-")))
                                    op = op1;

                                asignaciones.put(variableAsignada,"\n\t" + variableAsignada + " = " + op);
                            }
                            else {
                                Boolean asigne = false;
                                for(String k : asignaciones.keySet()) {//veo caso a = x + z y b = x + z -> b = a
                                    String asignacion = asignaciones.get(k);
                                    if(asignacion.contains(" = " + op1 + " " + operator + " " + op2)) {
                                        output += asignacion;
                                        asignaciones.put(variableAsignada, "\n\t" + variableAsignada + " = " + k);
                                        asignaciones.remove(k);
                                        asigne = true;
                                        break;
                                    }                                       
                                }       

                                if(!asigne){//cuando no pude operar por no tener disponible el valor de algun operando
                                    String aux = operoConVars(operator, op1, op2);
                                    asignaciones.put(variableAsignada,"\n\t" + variableAsignada + " = " + aux);
                                }
                            }
                        }
                    }
                    else {//var = x
                        if(variableAsignada.equals(op1))//x = x
                            continue;

                        if(vars.containsKey(op1)) {//es var que conozco el valor
                            value = vars.get(op1);
                            vars.put(variableAsignada, value);
                            asignaciones.put(variableAsignada, "\n\t" + variableAsignada + " = " + value);
                        }
                        else {
                            if(isTmpOrId(op1.charAt(0))) {//es tmp o id q no conozco valor
                                if(asignaciones.containsKey(op1)) {//reemplazo t = a op b -> x = t por x = a op b
                                    String tmp = asignaciones.get(op1).trim();
                                    output += "\n\t" + variableAsignada + " = " + tmp.substring(tmp.indexOf("=") + 1).trim();
                                    asignaciones.remove(op1);
                                }
                                else {//x = y -> marco y usada
                                    used.getLast().add(op1);
                                    asignaciones.put(variableAsignada, "\n" + l);
                                }
                            }
                            else {//es un numero
                                value = (int) Double.parseDouble(op1);
                                vars.put(variableAsignada, value);
                                asignaciones.put(variableAsignada, "\n\t" + variableAsignada + " = " + value);
                            }                                   
                        }
                    }
                }
                else if(l.contains("push")) {
                    String var = l.substring(l.trim().indexOf(" ") + 1).trim();
                    if(isTmpOrId(var.charAt(0))) {
                        if(asignaciones.containsKey(var)) {
                            output += asignaciones.get(var);
                            asignaciones.remove(var);
                        }
                        used.getLast().add(var);
                    }

                    output += "\n" + l;
                }
                else if(l.contains("pop")) {
                    output += "\n" + l;
                }
                else if(l.contains("lbl") || l.contains("jmp") || l.contains("ret")) {
                    for(String k : asignaciones.keySet()) //imprimo todas las asignaciones antes de salir del bloque
                        if(asignaciones.get(k) != null) {
                            noElimine = false;
                            output += asignaciones.get(k);
                            asignaciones.put(k, null);
                        }
                    
                    output += "\n" + l;

                    if(l.contains("ret"))
                        output += "\n";
                }
                else if(l.contains("ifz")) {
                    Integer val = 0;
                    String var = l.substring(4, l.indexOf("goto")).trim();

                    for(String k : asignaciones.keySet()) //imprimo todas las asignaciones antes de salir del bloque
                        if(asignaciones.get(k) != null) {
                            noElimine = false;
                            output += asignaciones.get(k);
                            asignaciones.put(k, null);
                        }

                    if(vars.containsKey(var)) {//si existe el valor reemplazo la variable
                        val = vars.get(var);
                        
                        String label = l.substring(l.indexOf("goto") + 5).trim();
                        output += "\n\tifz " + val + " goto " + label;
                    }
                    else {//es un num o no conozco el valor
                        if(isTmpOrId(var.charAt(0))) {//es tmp o id
                            if(asignaciones.containsKey(var)) {//imprimo el ultimo valor conocido de la variable
                                if(asignaciones.get(var) != null)    
                                    output += asignaciones.get(var);
                                asignaciones.remove(var);
                            }
                            used.getLast().add(var);
                        }                       
                        
                        output += "\n" + l;
                    }
                }                
            }

            if(noElimine) {
                for(String k : asignaciones.keySet()) //imprimo todas las asignaciones antes de salir del bloque
                    if(asignaciones.get(k) != null) {
                        output += asignaciones.get(k);
                        asignaciones.put(k, null);
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
    
    private static Boolean operacionNeutra(String operador, String x, String y) {
        if(operador.equals("*") && (x.equals("1") || y.equals("1")))
            return true;

        if(operador.equals("+") && (x.equals("0") || y.equals("0")))
            return true;

        if(operador.equals("/") && y.equals("1"))
            return true;

        if(operador.equals("-") && y.equals("0"))
            return true;

        return false;
    }

    private static String operoConVars(String operador, String x, String y) {
        String ret;

        if(x.equals(y) && (operador.equals("/") || operador.equals("-"))) {
            if(operador.equals("/"))
                ret = "1";
            else
                ret = "0";
        }
        else if((x.equals("2") || y.equals("2")) && operador.equals("*")) {
            if(x.equals("2"))
                ret = y + " + " + y;
            else
                ret = x + " + " + x;
        }
        else if(x.equals("0") && (operador.equals("/") || operador.equals("-"))) {
            if(operador.equals("/"))
                ret = "0";
            else
                ret = "-" + y;
        }
        else if((x.equals("0") || y.equals("0")) && operador.equals("*")) 
            ret = "0";
        else 
            ret = x + " " + operador + " " + y;

        return ret;
    }
}