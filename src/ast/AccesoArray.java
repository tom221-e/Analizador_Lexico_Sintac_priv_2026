package ast;

public class AccesoArray extends Expresion {
    private final String nombre;
    private final Expresion indice;

    public AccesoArray(String nombre, Expresion indice) {
        this.nombre = nombre;
        this.indice = indice;
    }

    @Override
    protected String getEtiqueta() {
        return "ACCESO ARRAY: " + nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        return super.graficar(idPadre) + indice.graficar(miId);
    }
}