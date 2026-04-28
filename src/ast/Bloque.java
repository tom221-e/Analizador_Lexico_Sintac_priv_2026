package ast;
import java.util.ArrayList;

public class Bloque extends Nodo {
    private final ArrayList<Sentencia> sentencias;

    public Bloque(ArrayList<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    @Override
    protected String getEtiqueta() {
        return "BLOQUE";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder(super.graficar(idPadre));
        for (Sentencia s : sentencias) {
            dot.append(s.graficar(miId));
        }
        return dot.toString();
    }
}