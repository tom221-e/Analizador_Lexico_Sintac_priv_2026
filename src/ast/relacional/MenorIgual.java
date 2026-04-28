package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;

public class MenorIgual extends OperacionBinaria {
    public MenorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "<=";
    }
}