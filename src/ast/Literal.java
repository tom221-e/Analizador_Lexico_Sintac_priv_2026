package ast;

public class Literal extends Expresion {
    private final String valor;
    private final String tipo;

    public Literal(String tipo, Object valor) {
        this.valor = String.valueOf(valor);
        this.tipo = tipo;
    }

    @Override
    protected String getEtiqueta() {
        return tipo + ": " + valor; // Etiqueta para el globo del grafo
    }
}