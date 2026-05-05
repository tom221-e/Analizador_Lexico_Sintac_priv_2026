package ast;

import java.util.ArrayList;

public class SentenciaAlt extends Sentencia {
    private final Expresion condicion;
    private final ArrayList<Sentencia> cuerpo; // Cambiado a ArrayList
    private final SentenciaAlt alternativa;

    public SentenciaAlt(Expresion condicion, ArrayList<Sentencia> cuerpo, SentenciaAlt alternativa) {
        this.condicion = condicion;
        this.cuerpo = cuerpo;
        this.alternativa = alternativa;
    }

    @Override
    protected String getNombreSentencia() {
        return "ALT_WHILE";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // 1. Graficar el nodo ALT_WHILE y conectarlo al padre
        dot.append(super.graficar(idPadre));

        // 2. Graficar la condición de esta alternativa
        if (condicion != null) {
            dot.append(condicion.graficar(miId));
        }

        // 3. Crear el nodo intermedio "BODY" para las sentencias de la alternativa
        if (cuerpo != null && !cuerpo.isEmpty()) {
            String idBody = "body_alt_" + miId;

            dot.append(String.format("%s [label=\"BODY\"];\n", idBody));
            dot.append(String.format("%s -> %s;\n", miId, idBody));

            // Graficar cada sentencia dentro del cuerpo de la alternativa
            for (Sentencia s : cuerpo) {
                if (s != null) {
                    dot.append(s.graficar(idBody));
                }
            }
        }

        // 4. Graficar la siguiente alternativa (recursividad)
        if (alternativa != null) {
            dot.append(alternativa.graficar(miId));
        }

        return dot.toString();
    }
}