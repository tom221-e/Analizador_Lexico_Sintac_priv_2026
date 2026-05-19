package ast;
import java.util.ArrayList;

public class Bloque extends Sentencia {
    private final ArrayList<Sentencia> sentencias;

    public Bloque(ArrayList<Sentencia> sentencias) {
        this.sentencias = sentencias;
    }

    // Este es el método que te pedía la clase abstracta Sentencia
    @Override
    protected String getNombreSentencia() {
        return "BLOQUE"; // Etiqueta que aparecerá en tu grafo DOT
    }

    // Método útil para que la clase Programa pueda obtener la lista
    public ArrayList<Sentencia> getSentencias() {
        return sentencias;
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
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // Recorremos secuencialmente todas las sentencias que componen este bloque
        if (this.sentencias != null) {
            for (Sentencia s : this.sentencias) {
                if (s != null) {
                    // Acumulamos el código LLVM de cada instrucción (ej: asignaciones, llamadas, ifs)
                    resultado.append(s.generarCodigo());
                }
            }
        }

        return resultado.toString();
    }
}