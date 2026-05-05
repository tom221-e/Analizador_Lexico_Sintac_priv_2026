package ast.literal;

import ast.Expresion;

public class IdLiteral extends Expresion {
    private final String valor;

    public IdLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "ID: " + valor; // Etiqueta para el globo del grafo
    }
}