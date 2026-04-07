package lexer;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Punto de entrada del analizador léxico.
 * Pregunta al usuario si desea leer desde consola o desde input_1.txt.
 * Si se lee desde consola, ingresar "FIN" para terminar la sesión.
 */


public class Main_lexer {

    private static final String INPUT_FILE = "./src/input_1.txt";

    public static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        System.out.println("=== Analizador Léxico ===");
        System.out.println("¿Desde dónde desea leer?");
        System.out.println("  1 - Desde consola");
        System.out.println("  2 - Desde archivo (" + INPUT_FILE + ")");
        System.out.print("Ingrese su opción: ");

        String opcion = teclado.nextLine().trim();

        Lexer lexico;
        try {
            if (opcion.equals("1")) {
                System.out.println("\nModo consola. Ingrese expresiones línea a línea.");
                System.out.println("Escriba FIN para terminar.\n");
                lexico = new Lexer(new InputStreamReader(System.in));
            } else if (opcion.equals("2")) {
                System.out.println("\nLeyendo desde: " + INPUT_FILE + "\n");
                lexico = new Lexer(new FileReader(INPUT_FILE));
            } else {
                System.out.println("Opción inválida. Saliendo.");
                return;
            }
            Token token;
            while ((token = lexico.yylex()) != null) {
                System.out.println("Token: " + token);

                if (token.nombre.equals("FIN")) {
                    System.out.println("\nToken FIN recibido. Terminando análisis.");
                    break;
                }
            }
            System.out.println("Análisis léxico terminado.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        finally {
            teclado.close();
        }
    }
}