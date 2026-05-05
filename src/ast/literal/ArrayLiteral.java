package ast.literal;

import ast.Expresion;

public class ArrayLiteral extends Expresion {
    private final String valor;

    public ArrayLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "ARRAY: " + valor; // Etiqueta para el globo del grafo
    }
}