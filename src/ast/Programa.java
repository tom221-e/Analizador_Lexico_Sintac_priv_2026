package ast;
import java.util.ArrayList;

public class Programa extends Nodo {
    private final String nombre;
    private final ArrayList<Declaracion> declaraciones; // Para lista_variables
    private final ArrayList<Sentencia> instrucciones;

    public Programa(String nombre, ArrayList<Declaracion> declaraciones, ArrayList<Sentencia> instrucciones) {
        this.nombre = nombre;
        this.declaraciones = declaraciones;
        this.instrucciones = instrucciones;
    }
    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder(super.graficar(idPadre));

        // Graficamos las declaraciones primero
        if (declaraciones != null) {
            for (Declaracion d : declaraciones) {
                dot.append(d.graficar(miId));
            }
        }

        // Luego las instrucciones
        if (instrucciones != null) {
            for (Sentencia s : instrucciones) {
                // Un chequeo extra por si alguna sentencia individual es null
                if (s != null) {
                    dot.append(s.graficar(miId));
                }
            }
        }
        return dot.toString();
    }
}