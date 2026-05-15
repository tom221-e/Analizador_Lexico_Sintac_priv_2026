package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;

public class OperacionOr extends OperacionBinaria {
    public OperacionOr(Expresion izquierda, Expresion derecha, SymbolTable tabla) {
        super(izquierda, derecha, tabla);
    }

    @Override
    protected String getNombreOperacion() {
        return "OR";
    }

    @Override
    public String get_llvm_op_code() {
        return "or";
    }
}