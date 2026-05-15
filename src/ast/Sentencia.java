package ast;

public abstract class Sentencia extends Expresion {
    @Override
    protected String getEtiqueta() {
        return getNombreSentencia();
    }

    protected abstract String getNombreSentencia();
}