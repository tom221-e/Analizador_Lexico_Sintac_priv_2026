package parser;

import ast.*;
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
                    grafico.println(astRoot.graficar());
                    grafico.close();

                    // Ejecutar Graphviz (comando 'dot')
                    Process processDot = Runtime.getRuntime().exec(new String[]{"dot", "-Tpng", "arbol.dot", "-o", "arbol.png"});

                    // Leer posibles errores del proceso 'dot'
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(processDot.getErrorStream()));
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("DOT Error: " + line);
                    }

                    System.out.println("AST generado exitosamente: arbol.dot y arbol.png");

                    // =========================================================================
                    // 🌟 BACKEND ACTIVADO: GENERACIÓN DE LLVM IR (.ll)
                    // =========================================================================
                    System.out.println("Generando código LLVM IR...");

                    File destinoLl = new File("codigo_salida.ll");
                    PrintWriter codigoLLVM = new PrintWriter(new FileWriter(destinoLl));

                    // Invocamos el método maestro que dispara la recursividad en todo el AST
                    codigoLLVM.println(astRoot.generarCodigo());
                    codigoLLVM.close();

                    System.out.println("Código LLVM IR generado exitosamente: " + destinoLl.getAbsolutePath());

                    // =========================================================================
                    // 🌟 NUEVO: COMPILACIÓN AUTOMÁTICA A NATIVO (.exe) CON CLANG
                    // =========================================================================
                    System.out.println("\nIniciando compilación nativa con Clang...");

                    // Definimos el nombre final del archivo ejecutable
                    String rutaAbsolutaExe = new File("programa.exe").getAbsolutePath();

                    // Buscamos la librería auxiliar en el directorio raíz /Funcion/array_alu.ll
                    String raizProyecto = System.getProperty("user.dir");
                    File archivoAlu = new File(raizProyecto + File.separator + "Funcion" + File.separator + "array_alu.ll");

                    System.out.println("Buscando soporte matemático en: " + archivoAlu.getAbsolutePath());

                    if (!archivoAlu.exists()) {
                        System.err.println("[!] ERROR CRÍTICO: No se encontró la función auxiliar en la ruta especificada.");
                        System.err.println("Por favor, asegúrate de tener la carpeta 'Funcion' en la raíz de tu entorno de ejecución.");
                        return;
                    }

                    // Preparamos el comando exacto para Clang
                    String[] comandoClang = {
                            "clang",
                            destinoLl.getAbsolutePath(),
                            archivoAlu.getAbsolutePath(),
                            "-o",
                            rutaAbsolutaExe
                    };

                    System.out.println("Ejecutando enlazador Clang...");
                    Process processClang = Runtime.getRuntime().exec(comandoClang);

                    // Capturamos y mostramos en consola cualquier advertencia o error que devuelva Clang
                    try (BufferedReader clangErrorReader = new BufferedReader(new InputStreamReader(processClang.getErrorStream()))) {
                        String lineaClang;
                        while ((lineaClang = clangErrorReader.readLine()) != null) {
                            System.err.println("Clang Log: " + lineaClang);
                        }
                    }

                    int codigoSalida = processClang.waitFor();
                    if (codigoSalida == 0) {
                        System.out.println("=== COMPILACIÓN COMPLETADA CON ÉXITO ===");
                        System.out.println("Ejecutable binario creado en: " + rutaAbsolutaExe);
                    } else {
                        System.err.println("[!] ERROR: Clang falló al enlazar los archivos. Código de salida: " + codigoSalida);
                    }
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