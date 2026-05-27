package ast;

import java.util.ArrayList;
import llvm.*;

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
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        String lblCondicion = CodeGeneratorHelper.getNewTag();
        String lblCuerpo = CodeGeneratorHelper.getNewTag();
        String lblSiguiente = CodeGeneratorHelper.getNewTag();
        String lblFin = CodeGeneratorHelper.getNewTag();

        // 🌟 CORRECCIÓN: Agregado el prefijo '%%' para el salto de etiqueta
        resultado.append(String.format("br label %%%s\n", lblCondicion));

        // --- BLOQUE 1: CONDICIÓN ---
        resultado.append(String.format("\n%s:\n", lblCondicion));
        resultado.append(this.condicion.generarCodigo());

        // 🌟 CORRECCIÓN: Agregados los prefijos '%%' en las etiquetas de bifurcación condicional
        resultado.append(String.format("br i1 %1$s, label %%%2$s, label %%%3$s\n",
                this.condicion.getIr_ref(), lblCuerpo, lblSiguiente));

        // --- BLOQUE 2: CUERPO ---
        resultado.append(String.format("\n%s:\n", lblCuerpo));
        if (this.cuerpo != null) {
            for (Sentencia s : this.cuerpo) {
                if (s != null) {
                    resultado.append(s.generarCodigo());
                }
            }
        }
        // 🌟 CORRECCIÓN: Agregado '%%'
        resultado.append(String.format("br label %%%s\n", lblFin));

        // --- BLOQUE 3: SIGUIENTE ALTERNATIVA ---
        resultado.append(String.format("\n%s:\n", lblSiguiente));
        if (this.alternativa != null) {
            resultado.append(this.alternativa.generarCodigo());
        }
        // 🌟 CORRECCIÓN: Agregado '%%'
        resultado.append(String.format("br label %%%s\n", lblFin));

        // --- BLOQUE 4: ETIQUETA DE FIN LOCAL ---
        resultado.append(String.format("\n%s:\n", lblFin));

        return resultado.toString();
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