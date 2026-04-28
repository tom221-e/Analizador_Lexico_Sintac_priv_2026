package ast;

public abstract class Sentencia extends Nodo {
    @Override
    protected String getEtiqueta() {
        return getNombreSentencia();
    }

    protected abstract String getNombreSentencia();
}