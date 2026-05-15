package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;

public class OperacionAnd extends OperacionBinaria {
    public OperacionAnd(Expresion izquierda, Expresion derecha, SymbolTable tabla) {
        super(izquierda, derecha, tabla);
    }

    @Override
    protected String getNombreOperacion() {
        return "AND";
    }

    @Override
    public String get_llvm_op_code() {
        return "and";
    }
}