package ast;

public abstract class OperacionUnaria extends Expresion {
    protected final Expresion operando;

    public OperacionUnaria(Expresion operando) {
        this.operando = operando;
    }

    @Override
    protected String getEtiqueta() {
        return getNombreOperacion();
    }

    protected abstract String getNombreOperacion();

    @Override
    protected String graficar(String idPadre) {
        final String miId = this.getId();
        return super.graficar(idPadre) + operando.graficar(miId);
    }
}