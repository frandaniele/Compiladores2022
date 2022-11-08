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
                            addVarWithValue(variableAsignada, value, vars, asignaciones);                            
                        }
                        else {
                            if(isTmpOrId(op1.charAt(0))) {//es tmp o id q no conozco valor
                                used.getLast().add(op1);
                                asignaciones.put(variableAsignada,"\n\t" + variableAsignada + " = -" + op1);
                            }
                            else {//es un numero
                                value = -(int) Double.parseDouble(op1);
                                addVarWithValue(variableAsignada, value, vars, asignaciones);
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
                            addVarWithValue(variableAsignada, value, vars, asignaciones);
                        }
                        else {//no pude obtener un resultado
                            if(operacionNeutra(operator, op1, op2)) {//resuelvo 1*x, 0+x etc
                                String op = op2;

                                if(op2.equals("1") && (operator.equals("*") || operator.equals("/")))//detecto cual es la variable para dejar
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
                            addVarWithValue(variableAsignada, value, vars, asignaciones);
                        }
                        else {
                            if(isTmpOrId(op1.charAt(0))) {//es tmp o id q no conozco valor
                                if(asignaciones.containsKey(op1)) {//reemplazo t = a op b, y x = t por x = a op b
                                    String tmp = asignaciones.get(op1).trim();
                                    asignaciones.put(variableAsignada, "\n\t" + variableAsignada + " = " + tmp.substring(tmp.indexOf("=") + 1).trim());
                                    asignaciones.remove(op1);
                                }
                                else {//x = y -> marco y usada
                                    used.getLast().add(op1);
                                    asignaciones.put(variableAsignada, "\n" + l);
                                }
                            }
                            else {//es un numero
                                value = (int) Double.parseDouble(op1);
                                addVarWithValue(variableAsignada, value, vars, asignaciones);
                            }                                   
                        }
                    }
                }
                else if(l.contains("push")) {
                    String var = l.substring(l.trim().indexOf(" ") + 1).trim();
                    output += getVarValue(var, asignaciones, used) + "\n" + l;//me fijo si tengo queimprimir asignacion y luego imprimi la linea
                }
                else if(l.contains("pop")) {
                    output += "\n" + l;
                }
                else if(l.contains("lbl") || l.contains("jmp") || l.contains("ret")) {
                    output += printAsignRestantes(asignaciones) + "\n" + l;//me fijo si tengo queimprimir asignaciones y luego imprimi la linea

                    if(l.contains("ret"))
                        output += "\n";
                }
                else if(l.contains("ifz")) {
                    String var = l.substring(4, l.indexOf("goto")).trim();

                    output += printAsignRestantes(asignaciones);

                    if(vars.containsKey(var)) {//si existe el valor reemplazo la variable
                        Integer val = vars.get(var);
                        
                        String label = l.substring(l.indexOf("goto") + 5).trim();
                        output += "\n\tifz " + val + " goto " + label;
                    }
                    else //es un num o no conozco el valor
                        output += getVarValue(var, asignaciones, used) + "\n" + l;//me fijo si tengo queimprimir asignacion y luego imprimi la linea                    
                }                
            }

            output += printAsignRestantes(asignaciones);//en algunos casos quedan sin imprimir
        }

        return output;
    }

    /**
     * recibe strings del operador y los operandos (numeros)
     * y realiza la operacion pertinente
     * 
     * @param operador
     * @param op1
     * @param op2
     * @return resultado de -> op1 operador op2
     */
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

    /**
     * recibo un string y detecto que operacion es
     * para devolver su operador
     * 
     * @param operacion
     * @return el operador
     */
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
    
    /**
     * detecto operaciones neutras de la matematica
     * 
     * @param operador
     * @param x
     * @param y
     * @return true si es operacion neutra, falso caso contrario
     */
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

    /**
     * cuando alguno de los operandos es una variable
     * determino cual sera el valor a asignar a una variable
     * 
     * @param operador
     * @param x
     * @param y
     * @return la operacion determinada
     */
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

    /**
     * cuando tengo una variable con el valro conocido
     * la agrego al mapa de vars con su valor
     * y al de vars con su linea de asignacion
     * 
     * @param name
     * @param value
     * @param nameVal
     * @param nameText
     */
    private static void addVarWithValue(String name, Integer value, HashMap<String, Integer> nameVal, HashMap<String, String> nameText) {
        nameVal.put(name, value);
        nameText.put(name, "\n\t" + name + " = " + value);
    }

    /**
     * si quedaron asignaciones sueltas 
     * (variables que no se usaron en el bloque basico que se asignaron
     * pero mas tarde si) las imprime
     * 
     * @param asignaciones
     * @return lo que debe imprimir
     */
    private static String printAsignRestantes(HashMap<String, String> asignaciones) {
        String ret = "";
        
        for(String k : asignaciones.keySet()) //imprimo todas las asignaciones antes de salir del bloque
            if(asignaciones.get(k) != null) {
                ret += asignaciones.get(k);
                asignaciones.put(k, null);//para no volverlas a imprimir
            }

        return ret;
    }

    /**
     * esta funcion se fija si debe imprimir 
     * la ultima asignacion de una variable
     * y la marca usada
     * en el caso de que se necesite
     * caso contrario no hace nada
     * 
     * @param var
     * @param asignaciones
     * @param used
     * @return string vacio o la ultima asignacion de la variable
     */
    private static String getVarValue(String var, HashMap<String, String> asignaciones, LinkedList<LinkedList<String>> used) {
        String ret = "";
        
        if(isTmpOrId(var.charAt(0))) {
            if(asignaciones.containsKey(var)) {
                if(asignaciones.get(var) != null) {
                    ret += asignaciones.get(var);
                    asignaciones.remove(var);
                }
            }
            used.getLast().add(var);
        }
    
        return ret;
    }
}