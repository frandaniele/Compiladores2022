package primerproyecto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class App 
{
    public static void main( String[] args )
    {
        CharStream input = null;
        try {
            input = CharStreams.fromFileName("input/declaraciones.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create a lexer that feeds off of input CharStream
        declaracionesLexer lexer = new declaracionesLexer(input);
        
        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        
        // create a parser that feeds off the tokens buffer
        declaracionesParser parser = new declaracionesParser(tokens);
                
        // create Listener
        declaracionesBaseListener escucha = new Escucha();

        // Conecto el objeto con Listeners al parser
        parser.addParseListener(escucha);

        // Solicito al parser que comience indicando una regla gramatical
        // En este caso la regla es el simbolo inicial
        ParseTree tree =  parser.programa();
        System.out.println("Termino el parseo.");
       
        // Conectamos el visitor
        if(((Escucha) escucha).getError()) {
            System.out.println("Sintaxis incorrecta. FIN");
        }
        else {
            Visitor visitor = new Visitor();
            visitor.visit(tree);
            System.out.println("Se genero el TAC.");
            
            Optimizador opt = new Optimizador();

            String code_opt1 = opt.Optimizar("tac");

            Integer optimizaciones = 0;
            while(optimizaciones < 8) {//hago pasadas hasta que no se optimice mas nada
                String code_opt2 = opt.Optimizar(code_opt1);
                optimizaciones++;

                try {
                    byte[] f1 = Files.readAllBytes(Paths.get(code_opt1));
                    byte[] f2 = Files.readAllBytes(Paths.get(code_opt2));
                    
                    code_opt1 = code_opt2;

                    if(Arrays.equals(f1,f2)) {//detecto que no optimiza mas y elimino el ultimo output
                        Files.delete(Paths.get(code_opt2));
                        optimizaciones = 10;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Termino la optimizacion.");
        }
    }
}