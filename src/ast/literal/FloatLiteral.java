package ast.literal;

import ast.Expresion;

public class FloatLiteral extends Expresion {
    private final String valor;

    public FloatLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "FLOAT: " + valor; // Etiqueta para el globo del grafo
    }
}