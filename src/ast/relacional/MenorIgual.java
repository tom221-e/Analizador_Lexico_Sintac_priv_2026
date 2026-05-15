package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class MenorIgual extends OperacionBinaria {
    public MenorIgual(Expresion izquierda, Expresion derecha, SymbolTable table) {
        super(izquierda, derecha, table);
    }

    @Override
    protected String getNombreOperacion() {
        return "<=";
    }
    @Override
    public String get_llvm_op_code() {
        ValidatorDataType validator = new ValidatorDataType();
        if ("FLOAT".equals(validator.obtenerInfo(this.izquierda, this.tabla).getTipo())) {
            return "fcmp ole"; // Ordered Less or Equal
        }
        return "icmp sle";     // Signed Less or Equal
    }
}