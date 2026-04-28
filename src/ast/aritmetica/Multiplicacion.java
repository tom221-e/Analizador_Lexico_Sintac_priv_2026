package ast.aritmetica;

import ast.Expresion;
import ast.OperacionBinaria;

public class Multiplicacion extends OperacionBinaria {
    public Multiplicacion(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "*"; // Corresponde a OP_MULTI
    }
}