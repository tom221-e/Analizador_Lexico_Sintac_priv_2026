package ast;

public class SentenciaIf extends Sentencia {
    private final Expresion condicion;
    private final Bloque cuerpoThen;
    private final Nodo sentenciaElse; // Puede ser otro SentenciaIf (un elif) o un Bloque (else)

    public SentenciaIf(Expresion condicion, Bloque cuerpoThen, Nodo sentenciaElse) {
        this.condicion = condicion;
        this.cuerpoThen = cuerpoThen;
        this.sentenciaElse = sentenciaElse;
    }

    @Override
    protected String getNombreSentencia() {
        return "IF";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        String dot = super.graficar(idPadre) +
                condicion.graficar(miId) +
                cuerpoThen.graficar(miId);
        if (sentenciaElse != null) {
            dot += sentenciaElse.graficar(miId);
        }
        return dot;
    }
}