package ast;

public class ConversionFloat extends Expresion {
    private final Expresion hijo;

    public ConversionFloat(Expresion hijo) {
        this.hijo = hijo;
    }

    @Override
    protected String getEtiqueta() {
        return "(INT_A_FLOAT)";
    }

    @Override
    public String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // Grafica este nodo (INT_A_FLOAT)
        dot.append(super.graficar(idPadre));

        // Grafica la conexión con el hijo (el ID original)
        if (hijo != null) {
            dot.append(hijo.graficar(miId));
        }

        return dot.toString();
    }
}