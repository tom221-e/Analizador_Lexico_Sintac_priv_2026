package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class Resta extends OperacionBinaria {

    public Resta(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "-"; // Token visual para el AST
    }

    @Override
    public String get_llvm_op_code() {
        if (this.tipo == null) {
            return "; ERROR: Tipo de dato nulo en la operación\n";
        }

        // Si el tipo es numérico (dimensión), le avisa al Padre que use la ALU de vectores
        if (this.tipo.matches("\\d+")) {
            return "2"; // Tu identificador único para la Resta en la ALU vectorial
        }

        switch (this.tipo) {
            case "i32":
                return "sub";   // Opcode plano para enteros de 32 bits
            case "float":
                return "fsub";  // Opcode plano para punto flotante
            default:
                return "sub";
        }
    }

    /**
     * Genera la instrucción escalar limpia de resta para LLVM.
     * Es ejecutada por el Padre únicamente si no se trata de un arreglo.
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder resultado = new StringBuilder();

        // 1. Solicitamos un nuevo registro temporal SSA para guardar el resultado de la resta
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 2. Mapeamos el tipo de emisión correcto según el entorno
        String tipoEmision;
        switch (this.tipo) {
            case "i32":
                tipoEmision = "i32";
                break;
            case "float":
            case "double":
                tipoEmision = "double"; // Forzamos double de 64 bits para tus operaciones matemáticas
                break;
            default:
                tipoEmision = "i32";
                break;
        }

        // 3. Emitimos la instrucción formateada de LLVM:
        // Formato: %ptro.X = sub i32 %ptro.A, %ptro.B
        // Formato: %ptro.Y = fsub double %ptro.C, %ptro.D
        resultado.append(String.format("  %1$s = %2$s %3$s %4$s, %5$s\n",
                this.getIr_ref(),
                this.get_llvm_op_code(),
                tipoEmision,
                this.izquierda.getIr_ref(),
                this.derecha.getIr_ref()
        ));

        return resultado.toString();
    }
}