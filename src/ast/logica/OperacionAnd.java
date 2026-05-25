package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class OperacionAnd extends OperacionBinaria {

    public OperacionAnd(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "AND"; // Token visual para el AST
    }

    @Override
    public String get_llvm_op_code() {
        return "and"; // Opcode nativo de LLVM para operaciones lógicas a nivel de bits (y booleanos)
    }

    /**
     * Genera la instrucción lógica escalar para el operador AND.
     * En LLVM, tanto los operandos de entrada como el de salida son de tipo 'i1' (1 bit).
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder resultado = new StringBuilder();

        // 1. Solicitamos un nuevo registro temporal SSA para guardar el resultado del AND (i1)
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 2. Al ser una operación lógica pura, el tipo de emisión en LLVM es estrictamente i1
        String tipoEmision = "i1";

        // 3. Emitimos la instrucción formateada de LLVM:
        // Formato: %ptro.X = and i1 %ptro.A, %ptro.B
        resultado.append(String.format("  %1$s = %2$s %3$s %4$s, %5$s\n",
                this.getIr_ref(),
                this.get_llvm_op_code(),
                tipoEmision,
                this.izquierda.getIr_ref().trim(),
                this.derecha.getIr_ref().trim()
        ));

        return resultado.toString();
    }
}