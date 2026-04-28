package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;

public class Suma extends OperacionBinaria {
    public Suma(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "+"; // Corresponde a OP_SUMA
    }
}