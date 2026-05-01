package ast;

public class SentenciaElif extends Sentencia {
    private final Expresion condicion;
    private final Bloque cuerpoThen;
    private final Sentencia sentenciaElse; // Puede ser otro SentenciaIf (un elif) o un Bloque (else)

    public SentenciaElif(Expresion condicion, Bloque cuerpoThen, Sentencia sentenciaElse) {
        this.condicion = condicion;
        this.cuerpoThen = cuerpoThen;
        this.sentenciaElse = sentenciaElse;
    }

    @Override
    protected String getNombreSentencia() {
        return "ELIF";
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