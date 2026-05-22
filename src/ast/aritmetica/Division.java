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
        return "sdiv";
    }
}