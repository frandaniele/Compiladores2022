package primerproyecto;

import java.io.IOException;

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
       
        // Conectamos el visitor
        if(((Escucha) escucha).getError()) {
            System.out.println("Sintaxis incorrecta. FIN");
        }
        else {
            Visitor visitor = new Visitor();
            visitor.visit(tree);
    
            Optimizador opt = new Optimizador();

            String code_opt = opt.Optimizar("tac");

            code_opt = opt.Optimizar(code_opt);//para hacer mas pasadas
        }
    }
}