package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;

public class Multiplicacion extends OperacionBinaria {
    public Multiplicacion(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "*"; // Corresponde a OP_MULTI
    }
    @Override
    public String get_llvm_op_code() {
        return "mul";
    }

}