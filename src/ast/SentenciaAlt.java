package ast;

public class SentenciaAlt extends Sentencia {
    private final Bloque cuerpo;

    public SentenciaAlt(Bloque cuerpo) {
        this.cuerpo = cuerpo;
    }

    @Override
    protected String getNombreSentencia() {
        return "ALT_WHILE";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        // Grafica el nodo "ALT_WHILE" y luego sus instrucciones internas
        return super.graficar(idPadre) + cuerpo.graficar(miId);
    }
}