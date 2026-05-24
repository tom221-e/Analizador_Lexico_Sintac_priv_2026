package parser;

import ast.*; // Asegúrate de importar tu clase raíz
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Scanner;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;
import lexer.Lexer;

public class Main_parser {

    public static void main(String[] args) throws Exception {

        File INPUT_PATH = new File("./src/input_files");
        Scanner teclado = new Scanner(System.in);

        System.out.println("=== Analizador Sintáctico ===");
        System.out.println("Seleccione un archivo:");

        File[] FILES = INPUT_PATH.listFiles();
        if (FILES == null || FILES.length == 0) {
            System.out.println("No se encontraron archivos en " + INPUT_PATH.getAbsolutePath());
            return;
        }

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
            Parser parser = new Parser(lexer, sf);

            try {
                // 1. Capturamos el objeto raíz del AST (Programa)
                Programa astRoot = (Programa) parser.parse().value;

                // 2. Imprimimos la tabla de símbolos
                parser.tablaSimbolos.print();
                System.out.println("Análisis sintáctico finalizado.");

                // 3. Generación del archivo .dot y la imagen .png
                if (astRoot != null) {
                    System.out.println("Generando gráfico del AST...");

                    // Escribir el contenido DOT
                    PrintWriter grafico = new PrintWriter(new FileWriter("arbol.dot"));
                    grafico.println(astRoot.graficar()); // Llamamos al método graficar de Programa
                    grafico.close();

                    // Ejecutar Graphviz (comando 'dot')
                    Process process = Runtime.getRuntime().exec(new String[]{"dot", "-Tpng", "arbol.dot", "-o", "arbol.png"});

                    // Leer posibles errores del proceso 'dot'
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("DOT Error: " + line);
                    }

                    System.out.println("AST generado exitosamente: arbol.dot y arbol.png");

                    // =========================================================================
                    // DESHABILITADO POR AHORA (COMENTADO)
                    // =========================================================================
                    System.out.println("Generando código LLVM IR...");

                    // Creamos el archivo de salida
                    PrintWriter codigoLLVM = new PrintWriter(new FileWriter("codigo_salida.ll"));

                    // Invocamos el método maestro que dispara la recursividad en todo el AST
                    codigoLLVM.println(astRoot.generarCodigo());
                    codigoLLVM.close();

                    System.out.println("Código LLVM IR generado exitosamente: codigo_salida.ll");
                    // =========================================================================
                }

            } catch (Exception e) {
                System.err.println("Error durante el análisis o generación de AST:");
                e.printStackTrace();
            }
        } else {
            System.out.println("Opción inválida.");
        }
    }
}