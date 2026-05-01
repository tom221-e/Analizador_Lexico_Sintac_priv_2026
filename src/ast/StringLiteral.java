package ast;

public class StringLiteral extends Nodo {
    private final String valor;

    public StringLiteral(String valor) {
        // Limpiamos las comillas para que el grafo se vea mejor
        this.valor = valor.replace("\"", "");
    }

    @Override
    protected String getEtiqueta() {
        return "STRING: " + valor; // Lo que dirá el globo en Graphviz
    }

    @Override
    protected String graficar(String idPadre) {
        // Llama a la lógica de Nodo para crear el círculo y la flecha
        return super.graficar(idPadre);
    }
}