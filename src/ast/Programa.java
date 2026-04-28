package ast;
import java.util.ArrayList;

public class Programa extends Nodo {
    private final String nombre;
    private final ArrayList<Sentencia> instrucciones;

    public Programa(String nombre, ArrayList<Sentencia> instrucciones) {
        this.nombre = nombre;
        this.instrucciones = instrucciones;
    }

    @Override
    protected String getEtiqueta() {
        return "PROGRAMA: " + nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder(super.graficar(idPadre));
        for (Sentencia s : instrucciones) {
            dot.append(s.graficar(miId));
        }
        return dot.toString();
    }
}