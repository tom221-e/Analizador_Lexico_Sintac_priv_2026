package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;

public class Division extends OperacionBinaria {
    public Division(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "/"; // Corresponde a OP_DIV
    }
}