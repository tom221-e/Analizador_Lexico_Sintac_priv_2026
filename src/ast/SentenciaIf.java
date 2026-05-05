package ast;

import java.util.ArrayList;

public class SentenciaIf extends Sentencia {
    private final Expresion condicion;
    private final ArrayList<Sentencia> cuerpoThen;
    private final ArrayList<Sentencia> sentenciaElse; // Puede ser otro SentenciaIf (un elif) o un Bloque (else)

    public SentenciaIf(Expresion condicion, ArrayList<Sentencia> cuerpoThen, ArrayList<Sentencia> sentenciaElse) {
        this.condicion = condicion;
        this.cuerpoThen = cuerpoThen;
        this.sentenciaElse = sentenciaElse;
    }

    @Override
    protected String getNombreSentencia() {
        return "IF";
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // 1. Nodo principal del IF
        dot.append(super.graficar(idPadre));
        dot.append(condicion.graficar(miId));

        // 2. Nodo THEN y sus hijos
        if (cuerpoThen != null && !cuerpoThen.isEmpty()) {
            String idThen = "then_" + miId;
            dot.append(String.format("%s [label=\"THEN\"];\n", idThen));
            dot.append(String.format("%s -> %s;\n", miId, idThen));
            for (Sentencia s : cuerpoThen) {
                if (s != null) dot.append(s.graficar(idThen));
            }
        }

        // 3. Nodo ELSE y sus hijos
        // (Aquí caerán tanto los bloques del ELSE como los nuevos IF creados por el ELIF)
        if (sentenciaElse != null && !sentenciaElse.isEmpty()) {
            String idElse = "else_" + miId;
            dot.append(String.format("%s [label=\"ELSE\"];\n", idElse));
            dot.append(String.format("%s -> %s;\n", miId, idElse));
            for (Sentencia s : sentenciaElse) {
                if (s != null) dot.append(s.graficar(idElse));
            }
        }

        return dot.toString();
    }
}