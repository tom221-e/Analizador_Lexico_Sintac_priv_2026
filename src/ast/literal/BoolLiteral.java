package ast.literal;

import ast.Expresion;

public class BoolLiteral extends Expresion {
    private final String valor;

    public BoolLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "Bool: " + valor; // Etiqueta para el globo del grafo
    }
}