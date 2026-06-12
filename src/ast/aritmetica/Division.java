package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class Division extends OperacionBinaria {

    public Division(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "/"; // Corresponde a OP_DIV para el árbol visual
    }

    @Override
    public String get_llvm_op_code() {
        if (this.tipo == null) {
            return "; ERROR: Tipo de dato nulo en la operación\n";
        }

        // Si el tipo es un número (ej: "10"), es la dimensión de un arreglo para el Padre
        if (this.tipo.matches("\\d+")) {
            return "3"; // ID único para la operación de División en tu ALU externa
        }

        switch (this.tipo) {
            case "INT":
                return "sdiv"; // Opcode plano para enteros de 32 bits
            case "FLOAT":
                return "fdiv"; // Opcode plano para punto flotante
            default:
                return "sdiv";
        }
    }

    /**
     * Reescritura del método protegido que invoca el Padre únicamente
     * en contextos escalares (no vectoriales).
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder resultado = new StringBuilder();

        // 1. Asignamos un puntero SSA nuevo para guardar el resultado de esta división
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 2. Mapeamos los tipos explícitos para la instrucción de LLVM con sus debidos breaks
        String tipoEmision;
        switch (this.tipo) {
            case "INT":
                tipoEmision = "i32";
                break;
            case "FLOAT":
                tipoEmision = "double"; // Forzamos double de 64 bits para tus operaciones matemáticas
                break;
            default:
                tipoEmision = "i32";
                break;
        }

        // 3. Formateamos la instrucción nativa limpia y bien estructurada:
        // Formato: %ptro.X = sdiv i32 %ptro.A, %ptro.B
        // Formato: %ptro.Y = fdiv double %ptro.C, %ptro.D
        resultado.append(String.format("  %1$s = %2$s %3$s %4$s, %5$s\n",
                this.getIr_ref(),               // %1$s -> Registro destino
                this.get_llvm_op_code(),        // %2$s -> "sdiv" o "fdiv"
                tipoEmision,                    // %3$s -> "i32" o "double"
                this.izquierda.getIr_ref(),     // %4$s -> Registro operando izquierdo
                this.derecha.getIr_ref()        // %5$s -> Registro operando derecho
        ));

        return resultado.toString();
    }
}