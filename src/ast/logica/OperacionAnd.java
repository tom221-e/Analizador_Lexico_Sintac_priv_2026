package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;

public class OperacionAnd extends OperacionBinaria {
    public OperacionAnd(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "AND";
    }
}