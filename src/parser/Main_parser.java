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
/*if (!parser.erroresSemanticos.isEmpty()) {
    System.err.println("\n=== ERRORES SEMÁNTICOS ENCONTRADOS ===");
    for (String error : parser.erroresSemanticos) {
        System.err.println(error);
    }
    System.err.println("Total: " + parser.erroresSemanticos.size() + " error(es).");
    return;  // no generar código si hay errores
}*/
            try {
                // 1. Capturamos el objeto raíz del AST (Programa)
                Programa astRoot = (Programa) parser.parse().value;
                
    // ← acá va el chequeo, DESPUÉS del parse()
    if (!parser.erroresSemanticos.isEmpty()) {
        System.err.println("\n=== ERRORES SEMÁNTICOS ENCONTRADOS ===");
        for (String error : parser.erroresSemanticos) {
            System.err.println(error);
        }
        System.err.println("Total: " + parser.erroresSemanticos.size() + " error(es).");
        return;  // no generar código si hay errores
    }

                // 2. Imprimimos la tabla de símbolos
                parser.tablaSimbolos.print();
                
                // línea nueva — genera el archivo ts.txt
                parser.tablaSimbolos.escribirArchivo("ts.txt");

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
                    // BACKEND: GENERACIÓN DE LLVM IR (.ll)
                    // =========================================================================
                    System.out.println("Generando código LLVM IR...");

                    File destinoLl = new File("codigo_salida.ll");
                    PrintWriter codigoLLVM = new PrintWriter(new FileWriter(destinoLl));

                    // Invocamos el método maestro que dispara la recursividad en todo el AST
                    codigoLLVM.println(astRoot.generarCodigo());

                    // PROTECCIÓN: Vaciamos y cerramos explícitamente para asegurar la persistencia en disco
                    codigoLLVM.flush();
                    codigoLLVM.close();

                    System.out.println("Código LLVM IR generado exitosamente: " + destinoLl.getAbsolutePath());

                    // =========================================================================
                    // COMPILACIÓN AUTOMÁTICA REQUERIDA POR LA CÁTEDRA (Paso A y Paso B)
                    // =========================================================================
                    System.out.println("\nIniciando secuencia de compilación nativa con Clang...");

                    String raizProyecto = System.getProperty("user.dir");

                    // Ambas dependencias se encuentran juntas en el directorio /Funcion
                    File archivoAlu = new File(raizProyecto + File.separator + "Funcion" + File.separator + "array_alu.ll");
                    File archivoScanfO = new File(raizProyecto + File.separator + "Funcion" + File.separator + "scanf.o");

                    File archivoObjetoSalida = new File("codigo_salida.o");
                    String rutaAbsolutaExe = new File("programa.exe").getAbsolutePath();

                    // Validaciones de archivos de soporte obligatorios
                    if (!archivoAlu.exists() || !archivoScanfO.exists()) {
                        System.err.println("[!] ERROR CRÍTICO: No se encontraron las dependencias en la carpeta '/Funcion'.");
                        System.err.println("Asegúrate de que 'array_alu.ll' y 'scanf.o' estén en: " + raizProyecto + File.separator + "Funcion");
                        return;
                    }

                    // -----------------------------------------------------------------
                    // PASO A: clang -c -o codigo_salida.o codigo_salida.ll
                    // -----------------------------------------------------------------
                    String[] comandoPasoA = {
                            "C:\\\\Program Files\\\\LLVM\\\\bin\\\\clang.exe", "-c",
                            "-o", archivoObjetoSalida.getAbsolutePath(),
                            destinoLl.getAbsolutePath()
                    };

                    System.out.println("Paso 1/2: Compilando código fuente a objeto (.o)...");
                    Process procPasoA = Runtime.getRuntime().exec(comandoPasoA);

                    // Captura de logs de error del Paso A
                    try (BufferedReader errorA = new BufferedReader(new InputStreamReader(procPasoA.getErrorStream()))) {
                        String log;
                        while ((log = errorA.readLine()) != null) {
                            System.err.println("Clang (Paso A) Log: " + log);
                        }
                    }

                    int statusA = procPasoA.waitFor();
                    if (statusA != 0) {
                        System.err.println("[!] Error en el Paso A. Compilación abortada.");
                        return;
                    }

                    // -----------------------------------------------------------------
                    // PASO B: clang -o programa.exe codigo_salida.o array_alu.ll scanf.o
                    // -----------------------------------------------------------------
                    String[] comandoPasoB = {
                            "C:\\\\Program Files\\\\LLVM\\\\bin\\\\clang.exe",
                            "-o", rutaAbsolutaExe,
                            archivoObjetoSalida.getAbsolutePath(),
                            archivoAlu.getAbsolutePath(),
                            archivoScanfO.getAbsolutePath(),
                            "-lmsvcrt",
                            "-llegacy_stdio_definitions"
                    };

                    System.out.println("Paso 2/2: Enlazando ejecutables y librerías auxiliares...");
                    Process procPasoB = Runtime.getRuntime().exec(comandoPasoB);

                    // Captura de logs de error del Paso B (Enlazador)
                    try (BufferedReader errorB = new BufferedReader(new InputStreamReader(procPasoB.getErrorStream()))) {
                        String log;
                        while ((log = errorB.readLine()) != null) {
                            System.err.println("Clang (Paso B) Log: " + log);
                        }
                    }

                    int statusB = procPasoB.waitFor();
                    if (statusB == 0) {
                        System.out.println("\n=== COMPILACIÓN COMPLETADA CON ÉXITO ===");
                        System.out.println("Ejecutable binario creado en: " + rutaAbsolutaExe);
                    } else {
                        System.err.println("[!] ERROR: El enlazador de Clang falló. Código de salida: " + statusB);
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