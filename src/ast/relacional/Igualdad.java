package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;

public class Igualdad extends OperacionBinaria {
    public Igualdad(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "=="; // Corresponde a DESIGUAL
    }
}
