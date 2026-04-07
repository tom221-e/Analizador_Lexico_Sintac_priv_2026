package parser;
import java.io.FileReader;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;
import lexer.Lexer;

/**
 *
 * @author itt
 */
public class Main_parser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        String path = "./src/input_1.txt";
        System.out.println("Análisis sintáctico iniciado:");
        FileReader fileReader = new FileReader(path);
        Lexer lexer = new Lexer(fileReader);
        
     
        SymbolFactory sf = new ComplexSymbolFactory();
        Parser parser= new Parser(lexer, sf);
        try{
            parser.parse();
            System.out.println("Análisis sintáctico finalizado.");}
        catch (Exception e){
            System.out.println(e.getMessage());
        };

    }
}