package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class Igualdad extends OperacionBinaria {
    public Igualdad(Expresion izquierda, Expresion derecha, SymbolTable tabla) {
        super(izquierda, derecha, tabla);
    }

    @Override
    protected String getNombreOperacion() {
        return "=="; // Corresponde a DESIGUAL
    }
    @Override
    public String get_llvm_op_code() {
        ValidatorDataType validator = new ValidatorDataType();
        if ("FLOAT".equals(validator.obtenerInfo(this.izquierda, this.tabla).getTipo())) {
            return "fcmp oeq"; // Ordered Equal
        }
        return "icmp eq";      // Equal
    }
}
