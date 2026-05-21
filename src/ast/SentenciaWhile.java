package ast;

import java.util.ArrayList;
import llvm.*;


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
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. CORRECCIÓN: Usar getNewTag() para las etiquetas de control del bloque
        String lblCondicion = CodeGeneratorHelper.getNewTag(); // Destino del CONTINUE (ej: tag.1)
        String lblCuerpo = CodeGeneratorHelper.getNewTag();    // El bloque de instrucciones (ej: tag.2)
        String lblFin = CodeGeneratorHelper.getNewTag();       // Destino del BREAK y salida (ej: tag.3)

        // =========================================================================
        // SOPORTE PARA BREAK Y CONTINUE: Apilamos las etiquetas en el helper
        // =========================================================================
        CodeGeneratorHelper.pushContinueTag(lblCondicion); // El continue volverá a evaluar
        CodeGeneratorHelper.pushBreakTag(lblFin);         // El break saltará inmediatamente afuera

        // 2. Salto inicial para entrar a la evaluación de la condición (lleva % en LLVM)
        resultado.append(String.format("br label %1$s\n", lblCondicion));

        // --- BLOQUE 1: EVALUACIÓN DE LA CONDICIÓN ---
        resultado.append(String.format("\n%1$s:\n", lblCondicion));
        resultado.append(this.condicion.generarCodigo());

        // Saltamos al cuerpo si es verdadero o al fin si es falso (lleva % antes de las etiquetas)
        resultado.append(String.format("br i1 %1$s, label %1$s, label %1$s\n",
                this.condicion.getIr_ref(), lblCuerpo, lblFin));

        // --- BLOQUE 2: CUERPO DEL CICLO ---
        resultado.append(String.format("\n%1$s:\n", lblCuerpo));

        if (this.cuerpo != null) {
            for (Sentencia s : this.cuerpo) {
                if (s != null) {
                    resultado.append(s.generarCodigo());
                    // Si alguna 's' interna es un SentenciaBreak o SentenciaContinue,
                    // leerá del helper 'lblFin' o 'lblCondicion' automáticamente.
                }
            }
        }

        // Al terminar el cuerpo, saltamos de regreso a la condición
        resultado.append(String.format("br label %1$s\n", lblCondicion));

        // =========================================================================
        // SOPORTE PARA BREAK Y CONTINUE: Desapilamos al salir de este contexto
        // =========================================================================
        CodeGeneratorHelper.popContinueTag();
        CodeGeneratorHelper.popBreakTag();

        // --- BLOQUE 3: SALIDA DEL CICLO ---
        resultado.append(String.format("\n%1$s:\n", lblFin));

        // NOTA: Si en un futuro necesitas que funcione tu 'alternativa' (SentenciaAlt)
        // en la generación de código, se procesaría aquí abajo, justo en el bloque de salida.

        return resultado.toString();
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