package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;

public class Division extends OperacionBinaria {
    public Division(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }
    
    @Override
    protected String getNombreOperacion() {
        return "/"; // Corresponde a OP_DIV
    }
    @Override
    public String get_llvm_op_code() {
        // Verificamos por seguridad que el tipo no sea nulo
        if (this.tipo == null) {
            return "; ERROR: Tipo de dato nulo en la operación\n";
        }

        switch (this.tipo) {
            case "i32":
                return "sdiv"; // Operación nativa para enteros de 32 bits

            case "float":
                return "fdiv"; // Operación nativa para flotantes de precisión simple

            default:
                return "3";
        }
    }
}