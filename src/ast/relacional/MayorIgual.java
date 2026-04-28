package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;

public class MayorIgual extends OperacionBinaria {
    public MayorIgual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "=>";
    }
}