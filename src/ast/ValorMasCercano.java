package ast;

public class ValorMasCercano extends Expresion {
    private final String idArray;
    private final Expresion referencia;

    public ValorMasCercano(String idArray, Expresion referencia) {
        this.idArray = idArray;
        this.referencia = referencia;
    }

    @Override
    protected String getEtiqueta() {
        return "VALOR_MAS_CERCANO (" + idArray + ")";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        // Grafica el nodo de la función y su hijo (la expresión de referencia)
        return super.graficar(idPadre) + referencia.graficar(miId);
    }
}