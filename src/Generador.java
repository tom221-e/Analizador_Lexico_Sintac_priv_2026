import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Genera MiLexico.java a partir de lexico.flex (JFlex)
 * y MiParser.java + MiParserSym.java a partir de parser.cup (java-cup).
 *
 * Ejecutar este main ANTES de compilar el proyecto si se modifican
 * lexico.flex o parser.cup.
 */
public class Generador {

    public static void main(String[] args) {
        generarLexer("./src/lexer/lexico.flex");
        generarParser(new String[]{
            "-destdir", "./src/parser",
            "./src/parser/parser.cup"
        });
    }

    public static void generarLexer(String path) {
        System.out.println("Generando léxico desde: " + path);
        File file = new File(path);
        jflex.generator.LexGenerator generator = new jflex.generator.LexGenerator(file);
        generator.generate();
        System.out.println("MiLexico.java generado correctamente.");
    }

    public static void generarParser(String[] params) {
        System.out.println("Generando parser desde: " + params[params.length - 1]);
        try {
            java_cup.Main.main(params);
            System.out.println("Parser.java y ParserSym.java generados correctamente.");
        } catch (IOException ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Generador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
