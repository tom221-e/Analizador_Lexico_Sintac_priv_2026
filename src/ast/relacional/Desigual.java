package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class Desigual extends OperacionBinaria {
    private SymbolTable tabla;
    
    public Desigual(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "!="; // Corresponde a DESIGUAL
    }
    @Override
    public String get_llvm_op_code() {
        ValidatorDataType validator = new ValidatorDataType();

        ValidatorDataType.InfoNodo info =
                validator.obtenerInfo(this.izquierda, this.tabla);

        if ("FLOAT".equals(info.getTipo())) {
            return "fcmp one double";
        }

        return "icmp ne";
    }
}