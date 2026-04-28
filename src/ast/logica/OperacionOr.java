package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;

public class OperacionOr extends OperacionBinaria {
    public OperacionOr(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "OR";
    }
}