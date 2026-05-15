package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import parser.SymbolTable;
import validator.ValidatorDataType;

public class Mayor extends OperacionBinaria {
    public Mayor(Expresion izquierda, Expresion derecha, SymbolTable table) {
        super(izquierda, derecha, table);
    }

    @Override
    protected String getNombreOperacion() {
        return ">";
    }
    @Override
    public String get_llvm_op_code() {
        // 1. Instanciamos el validador (simple, sin vueltas)
        ValidatorDataType validator = new ValidatorDataType();

        ValidatorDataType.InfoNodo info = validator.obtenerInfo(this.izquierda, tabla);

        // 3. Retornamos el comando según el tipo que detectó el validador
        if ("FLOAT".equals(info.getTipo())) {
            return "fcmp ogt";
        } else {
            return "icmp sgt";
        }
    }
}