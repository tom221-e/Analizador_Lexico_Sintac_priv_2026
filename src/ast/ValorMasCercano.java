package ast;

public class ValorMasCercano extends Expresion {
    private final String lista; // Cambiado de Expresion a String
    private final Expresion referencia;

    public ValorMasCercano(Expresion referencia, String lista) {
        this.referencia = referencia;
        this.lista = lista;
    }

    @Override
    protected String getEtiqueta() {
        // Incluimos el nombre de la lista en la etiqueta del nodo
        return "VALOR_MAS_CERCANO (Lista: " + lista + ")";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        // Solo graficamos la flecha hacia la referencia
        // El nombre de la lista ya aparece en el texto del nodo actual
        return super.graficar(idPadre) +
                referencia.graficar(miId);
    }
}