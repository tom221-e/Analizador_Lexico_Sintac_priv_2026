package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;

public class Menor extends OperacionBinaria {
    public Menor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "<";
    }
}
