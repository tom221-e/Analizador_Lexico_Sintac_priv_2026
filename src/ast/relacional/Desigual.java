package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;

public class Desigual extends OperacionBinaria {
    public Desigual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "!="; // Corresponde a DESIGUAL
    }
}