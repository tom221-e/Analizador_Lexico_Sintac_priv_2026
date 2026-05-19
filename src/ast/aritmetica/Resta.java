package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;

public class Resta extends OperacionBinaria {

    public Resta(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
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