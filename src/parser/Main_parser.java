package parser;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;
import lexer.Lexer;

public class Main_parser {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        File INPUT_PATH = new File("./src/input_files");

        Scanner teclado = new Scanner(System.in);

        System.out.println("=== Analizador Sintáctico ===");
        System.out.println("Seleccione un archivo:");

        File[] FILES = INPUT_PATH.listFiles();
        for (int i = 0; i < FILES.length; i++) {
            System.out.println(" " + (i + 1) + " - " + FILES[i].getName());
        }

        System.out.print("Ingrese el número del archivo: ");

        int seleccion = Integer.parseInt(teclado.nextLine());

        if (seleccion > 0 && seleccion <= FILES.length) {
            File archivo = FILES[seleccion - 1];
            System.out.println("\nLeyendo desde: " + archivo + "\n");

            System.out.println("Análisis sintáctico iniciado:");
            FileReader fileReader = new FileReader(archivo);
            Lexer lexer = new Lexer(fileReader);

            SymbolFactory sf = new ComplexSymbolFactory();
            Parser parser= new Parser(lexer, sf);
            try{
                parser.parse();
                parser.tablaSimbolos.print();
                System.out.println("Análisis sintáctico finalizado.");}
            catch (Exception e){
                System.out.println(e.getMessage());
            };
        } else {
            System.out.println("Opción inválida.");
        }

    }
}