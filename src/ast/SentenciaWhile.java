package ast;

import java.util.ArrayList;

public class SentenciaWhile extends Sentencia {
    private final Expresion condicion;
    private final ArrayList<Sentencia> cuerpo; // Cambiado de Bloque a ArrayList
    private final SentenciaAlt alternativa;

    public SentenciaWhile(Expresion condicion, ArrayList<Sentencia> cuerpo, SentenciaAlt alternativa) {
        this.condicion = condicion;
        this.cuerpo = cuerpo;
        this.alternativa = alternativa;
    }

    @Override
    protected String getNombreSentencia() {
        return "WHILE";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // 1. Graficar el nodo principal WHILE y su condición
        dot.append(super.graficar(idPadre));
        if (condicion != null) {
            dot.append(condicion.graficar(miId));
        }

        // 2. Crear el nodo intermedio "BODY" para las sentencias del cuerpo
        if (cuerpo != null && !cuerpo.isEmpty()) {
            String idBody = "body_" + miId;

            // Creamos el globo visual "BODY"
            dot.append(String.format("%s [label=\"BODY\"];\n", idBody));
            dot.append(String.format("%s -> %s;\n", miId, idBody));

            // Graficamos cada sentencia de la lista dentro del nodo BODY
            for (Sentencia s : cuerpo) {
                if (s != null) {
                    dot.append(s.graficar(idBody));
                }
            }
        }

        // 3. Graficar la alternativa (si existe)
        if (alternativa != null) {
            dot.append(alternativa.graficar(miId));
        }

        return dot.toString();
    }
}