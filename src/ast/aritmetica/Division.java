package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;

public class Division extends OperacionBinaria {
    public Division(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "/"; // Corresponde a OP_DIV
    }
    @Override
    public String get_llvm_op_code() {
        return "sdiv";
    }
}