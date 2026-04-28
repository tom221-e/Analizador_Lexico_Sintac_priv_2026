package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;

public class Mayor extends OperacionBinaria {
    public Mayor(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return ">";
    }
}