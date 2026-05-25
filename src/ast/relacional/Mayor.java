package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class Mayor extends OperacionBinaria {

    private SymbolTable tabla;
    public Mayor(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return ">";
    }  
    
    @Override
    public String get_llvm_op_code() {

        ValidatorDataType validator = new ValidatorDataType();

        ValidatorDataType.InfoNodo info =
                validator.obtenerInfo(derecha, tabla);

        if ("FLOAT".equals(info.getTipo())) {
            return "fcmp ogt double";
        }

        return "icmp sgt";
    }
}