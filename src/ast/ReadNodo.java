package ast;

public class ReadNodo extends Expresion {
    private final String tipoLectura;

    public ReadNodo(String tipoLectura) {
        this.tipoLectura = tipoLectura;
    }

    @Override
    protected String getEtiqueta() {
        return "READ: " + tipoLectura; // Ej: "READ: INT" o "READ: FLOAT"
    }

    @Override
    protected String graficar(String idPadre) {
        // Al ser un nodo hoja (no tiene hijos), solo se grafica a sí mismo
        return super.graficar(idPadre);
    }
}