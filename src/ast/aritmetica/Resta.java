package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;

public class Resta extends OperacionBinaria {

    public Resta(Expresion izquierda, Expresion derecha, SymbolTable tabla) {
        super(izquierda, derecha, tabla);
    }

    @Override
    protected String getNombreOperacion() {
        return "-";
    }
    @Override
    public String get_llvm_op_code() {
        return "sub";
    }
}