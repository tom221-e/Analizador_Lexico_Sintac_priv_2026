package ast.literal;

import ast.Expresion;

public class IntLiteral extends Expresion {
    private final String valor;

    public IntLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "INT: " + valor; // Etiqueta para el globo del grafo
    }
}