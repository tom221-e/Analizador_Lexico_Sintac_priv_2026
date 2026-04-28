package ast;

public class AsignacionArray extends Sentencia {
    private final String nombre;
    private final Expresion indice;
    private final Expresion valor;

    public AsignacionArray(String nombre, Expresion indice, Expresion valor) {
        this.nombre = nombre;
        this.indice = indice;
        this.valor = valor;
    }

    @Override
    protected String getNombreSentencia() {
        return "ASIGNACION ARRAY: " + nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        return super.graficar(idPadre) +
                indice.graficar(miId) +
                valor.graficar(miId);
    }
}