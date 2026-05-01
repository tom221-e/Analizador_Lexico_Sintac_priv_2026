package ast;

public class ValorMasCercano extends Expresion {
    private final Expresion lista;      // Ahora es un objeto Expresion, no un String
    private final Expresion referencia;

    public ValorMasCercano(Expresion referencia, Expresion lista) {
        this.referencia = referencia;
        this.lista = lista;
    }

    protected String getNombreSentencia() {
        return "VALOR_MAS_CERCANO"; // El globo principal
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        // Graficamos el nodo actual y colgamos ambos hijos
        return super.graficar(idPadre) +
                referencia.graficar(miId) +
                lista.graficar(miId);
    }
}