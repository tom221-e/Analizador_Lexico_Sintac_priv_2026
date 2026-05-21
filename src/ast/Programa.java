package ast;
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
        StringBuilder llvm = new StringBuilder();

        // =========================================================================
        // 1. CABECERA GLOBAL Y DECLARACIONES OBLIGATORIAS
        // =========================================================================
        llvm.append("; --- Cabecera del Programa ---\n");
        llvm.append("declare i32 @printf(i8*, ...)\n");
        llvm.append("declare i32 @scanf(i8*, ...)\n\n");

        // Formatos fijos para Print y Read (Obligatorios)
        llvm.append("@.integer = private constant [4 x i8] c\"%d\\0A\\00\"\n");
        llvm.append("@.float = private constant [4 x i8] c\"%f\\0A\\00\"\n");
        llvm.append("@int_read_format = unnamed_addr constant [3 x i8] c\"%d\\00\"\n");
        llvm.append("@float_read_format = unnamed_addr constant [3 x i8] c\"%f\\0A\\00\"\n\n");

        // =========================================================================
        // 2. DECLARACIÓN DE VARIABLES GLOBALES (Si tu lenguaje las usa aquí)
        // =========================================================================
        if (this.declaraciones != null) {
            llvm.append("; --- Declaraciones de Variables ---\n");
            for (Declaracion d : declaraciones) {
                if (d != null) {
                    llvm.append(d.generarCodigo());
                }
            }
            llvm.append("\n");
        }

        // =========================================================================
        // 3. CUERPO PRINCIPAL (Función @main)
        // =========================================================================
        llvm.append("; --- Función Principal ---\n");
        llvm.append("define i32 @main() {\n");
        llvm.append("entry:\n"); // Bloque de entrada obligatorio

        // Generamos el código de cada instrucción/sentencia dentro del main
        if (this.instrucciones != null) {
            for (Sentencia s : instrucciones) {
                if (s != null) {
                    llvm.append(s.generarCodigo());
                }
            }
        }

        // Cierre obligatorio de la función main con retorno 0
        llvm.append("  ret i32 0\n");
        llvm.append("}\n");

        return llvm.toString();
    }
}