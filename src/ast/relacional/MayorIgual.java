package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class MayorIgual extends OperacionBinaria {

    private SymbolTable tabla;
    public MayorIgual(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "=>";
    }
    @Override
    public String get_llvm_op_code() {
        ValidatorDataType validator = new ValidatorDataType();
        if ("FLOAT".equals(validator.obtenerInfo(this.izquierda, this.tabla).getTipo())) {
            return "fcmp oge"; // Ordered Greater or Equal
        }
        return "icmp sge";     // Signed Greater or Equal
    }
}