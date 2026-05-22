package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;
import parser.SymbolTable;


public class OperacionAnd extends OperacionBinaria {
    public OperacionAnd(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
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