package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class Suma extends OperacionBinaria {

    public Suma(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "+"; // Corresponde a OP_SUMA
    }

    @Override
    public String get_llvm_op_code() {
        if (this.tipo == null) {
            return "; ERROR: Tipo de dato nulo en la operación\n";
        }

        // Si el tipo es numérico (dimensión), le avisa al Padre que use la ALU de vectores
        if (this.tipo.matches("\\d+")) {
            return "1"; // Tu identificador único para la Suma en la ALU vectorial
        }

        switch (this.tipo) {
            case "i32":
                return "add";   // Opcode plano para enteros de 32 bits
            case "float":
            case "double":
                return "fadd";  // Opcode plano para punto flotante
            default:
                return "add";
        }
    }

    /**
     * Genera la instrucción escalar limpia de suma para LLVM.
     * Es ejecutada por el Padre únicamente si no se trata de un arreglo.
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder resultado = new StringBuilder();

        // 1. Solicitamos un nuevo registro temporal SSA para guardar el resultado de la suma
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 2. Mapeamos el tipo de emisión correcto según el entorno
        String tipoEmision;
        switch (this.tipo) {
            case "i32":
                tipoEmision = "i32";
                break;
                case "float":
                tipoEmision = "double"; // Forzamos double de 64 bits para mantener tu estándar de alta precisión
                break;
            default:
                tipoEmision = "i32";
                break;
        }

        // 3. Emitimos la instrucción formateada de LLVM:
        // Formato: %ptro.X = add i32 %ptro.A, %ptro.B
        // Formato: %ptro.Y = fadd double %ptro.C, %ptro.D
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