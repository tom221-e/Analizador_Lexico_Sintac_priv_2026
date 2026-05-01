package ast;

public class SentenciaAlt extends Sentencia {
    private final Expresion condicion;
    private final Bloque cuerpo;
    private final SentenciaAlt alternativa; // Aquí se usa la clase creada arriba

    public SentenciaAlt(Expresion condicion, Bloque cuerpo, SentenciaAlt alternativa) {
        this.condicion = condicion;
        this.cuerpo = cuerpo;
        this.alternativa = alternativa;
    }

    @Override
    protected String getNombreSentencia() {
        return "ALT_WHILE";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        String dot = super.graficar(idPadre) +
                condicion.graficar(miId) +
                cuerpo.graficar(miId);

        // Solo grafica la alternativa si no es nula
        if (alternativa != null) {
            dot += alternativa.graficar(miId);
        }
        return dot;
    }
}