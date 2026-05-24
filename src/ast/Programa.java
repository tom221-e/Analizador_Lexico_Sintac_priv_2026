package ast;
import llvm.CodeGeneratorHelper;
import parser.Parser;

import java.util.ArrayList;

public class Programa extends Nodo {
    private final String nombre;
    private final ArrayList<Declaracion> declaraciones; // Para lista_variables
    private final ArrayList<Sentencia> instrucciones;

    public Programa(String nombre, ArrayList<Declaracion> declaraciones, ArrayList<Sentencia> instrucciones) {
        this.nombre = nombre;
        this.declaraciones = declaraciones;
        this.instrucciones = instrucciones;
    }

    public String graficar() {
        return "digraph AST {\n" +
                "node [shape=circle];\n" +
                this.graficar(null) + // Llama al método protegido iniciando la raíz
                "\n}";
    }

    @Override
    protected String getEtiqueta() {
        return "PROGRAMA: " + nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder(super.graficar(idPadre));

        // Graficamos las declaraciones primero
        if (declaraciones != null) {
            for (Declaracion d : declaraciones) {
                dot.append(d.graficar(miId));
            }
        }

        // Luego las instrucciones
        if (instrucciones != null) {
            for (Sentencia s : instrucciones) {
                // Un chequeo extra por si alguna sentencia individual es null
                if (s != null) {
                    dot.append(s.graficar(miId));
                }
            }
        }
        return dot.toString();
    }

    @Override
    public String generarCodigo() {
        // =========================================================================
        // 1. PROCESAR INSTRUCCIONES PRIMERO (Llena el almacén de textos globales)
        // =========================================================================
        StringBuilder cuerpoInstrucciones = new StringBuilder();
        if (this.instrucciones != null) {
            for (Sentencia s : instrucciones) {
                if (s != null) {
                    cuerpoInstrucciones.append(s.generarCodigo());
                }
            }
        }

        StringBuilder llvm = new StringBuilder();

        // =========================================================================
        // 2. CABECERA GLOBAL Y DECLARACIONES OBLIGATORIAS
        // =========================================================================
        llvm.append("; --- Cabecera del Programa ---\n");
        llvm.append("declare i32 @printf(i8*, ...)\n");
        llvm.append("declare i32 @scanf(i8*, ...)\n\n");
        llvm.append("declare void @operar_arreglos(float*, float*, float*, i32, i32)\n\n");

        // Formatos fijos para Print y Read (Obligatorios)
        llvm.append("@.integer = private constant [4 x i8] c\"%d\\0A\\00\"\n");
        llvm.append("@.float = private constant [4 x i8] c\"%f\\0A\\00\"\n");
        llvm.append("@int_read_format = unnamed_addr constant [3 x i8] c\"%d\\00\"\n");
        llvm.append("@float_read_format = unnamed_addr constant [3 x i8] c\"%f\\0A\\00\"\n\n");

        // =========================================================================
        // 3. CADENAS DE TEXTO DINÁMICAS (ALMACÉN GLOBAL)
        // =========================================================================
        String textosGlobales = CodeGeneratorHelper.obtenerConstantesGlobales();
        if (textosGlobales != null && !textosGlobales.isEmpty()) {
            llvm.append("; --- Cadenas de Texto Globales ---\n");
            llvm.append(textosGlobales);
            llvm.append("\n");
        }

        // =========================================================================
        // 4. FUNCIÓN PRINCIPAL Y ENTRADA DE PILA (entry:)
        // =========================================================================
        llvm.append("; --- Función Principal ---\n");
        llvm.append("define i32 @main() {\n");
        llvm.append("entry:\n");

        llvm.append("  ; --- Declaraciones de Variables Locales (Pila de Main) ---\n");

        // LinkedHashSet filtra automáticamente líneas idénticas y mantiene el orden físico
        java.util.LinkedHashSet<String> lineasDeclaracion = new java.util.LinkedHashSet<>();

        // Procesamos la lista unificada (variables nativas + variables de la macro agregadas en CUP)
        if (this.declaraciones != null) {
            for (Declaracion d : declaraciones) {
                if (d != null) {
                    // Separamos por saltos de línea el código generado por cada Declaración
                    String[] lineas = d.generarCodigo().split("\n");
                    for (String l : lineas) {
                        // Limpiamos los retornos de carro (\r) y espacios extras
                        String lineaLimpia = l.replace("\r", "").trim();
                        if (!lineaLimpia.isEmpty()) {
                            lineasDeclaracion.add(lineaLimpia); // Si está repetida, el Set la ignora
                        }
                    }
                }
            }
        }

        // Escribimos todas las instrucciones 'alloca' y 'store' iniciales una sola vez
        for (String linea : lineasDeclaracion) {
            llvm.append("  ").append(linea).append("\n");
        }
        llvm.append("\n");

        // =========================================================================
        // 5. CUERPO DE EJECUCIÓN
        // =========================================================================
        llvm.append("  ; --- Cuerpo de Ejecución ---\n");
        llvm.append(cuerpoInstrucciones.toString());

        // Cierre formal de la función @main
        llvm.append("  ret i32 0\n");
        llvm.append("}\n");

        return llvm.toString();
    }
}