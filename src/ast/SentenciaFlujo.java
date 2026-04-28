package ast;

public class SentenciaFlujo extends Sentencia {
    private final String tipo; // "BREAK" o "CONTINUE"

    public SentenciaFlujo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    protected String getNombreSentencia() {
        return tipo;
    }
}