package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class Desigual extends OperacionBinaria {
    public Desigual(Expresion izquierda, Expresion derecha, SymbolTable tabla) {
        super(izquierda, derecha, tabla);
    }

    @Override
    protected String getNombreOperacion() {
        return "!="; // Corresponde a DESIGUAL
    }
    @Override
    public String get_llvm_op_code() {
        ValidatorDataType validator = new ValidatorDataType();
        if ("FLOAT".equals(validator.obtenerInfo(this.izquierda, this.tabla).getTipo())) {
            return "fcmp one"; // Ordered Not Equal
        }
        return "icmp ne";      // Not Equal
    }
}