package ast;

public class SentenciaPrint extends Sentencia {
    private final Nodo contenido; // Puede ser una Expresion o un StringLiteral

    public SentenciaPrint(Nodo contenido) {
        this.contenido = contenido;
    }

    @Override
    protected String getNombreSentencia() {
        return "PRINT";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        return super.graficar(idPadre) +
                (contenido != null ? contenido.graficar(miId) : "");
    }
}