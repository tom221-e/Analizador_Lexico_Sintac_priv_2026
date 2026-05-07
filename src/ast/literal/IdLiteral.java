package ast.literal;

import ast.Expresion;

public class IdLiteral extends Expresion {
    private final String valor;

    public IdLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    // CAMBIO AQUÍ: Renombrar para no sobreescribir al padre
    public String getNombreVariable() {
        return valor;
    }

    @Override
    protected String getEtiqueta() {
        return "ID: " + valor;
    }
}