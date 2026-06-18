package ast;

import java.util.ArrayList;
import llvm.*;

public class SentenciaWhile extends Sentencia {
    private final Expresion condicion;
    private final ArrayList<Sentencia> cuerpo;
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

        String lblCondicion = CodeGeneratorHelper.getNewTag();
        String lblCuerpo = CodeGeneratorHelper.getNewTag();
        String lblEvaluarAlt = CodeGeneratorHelper.getNewTag(); //NUEVO: Bloque intermedio para controlar el alt_while
        String lblFin = CodeGeneratorHelper.getNewTag();

        // SOPORTE PARA BREAK Y CONTINUE
        CodeGeneratorHelper.pushContinueTag(lblCondicion);
        CodeGeneratorHelper.pushBreakTag(lblFin);

        // Salto inicial al bloque de condición
        resultado.append(String.format("br label %%%1$s\n", lblCondicion));

        // --- BLOQUE 1: EVALUACIÓN DE LA CONDICIÓN ---
        resultado.append(String.format("\n%1$s:\n", lblCondicion));
        resultado.append(this.condicion.generarCodigo());

        // CORRECCIÓN: Si es falso, saltamos a evaluar las alternativas (lblEvaluarAlt) en lugar de ir al fin directo
        resultado.append(String.format("br i1 %1$s, label %%%2$s, label %%%3$s\n",
                this.condicion.getIr_ref(), lblCuerpo, lblEvaluarAlt));

        // --- BLOQUE 2: CUERPO DEL CICLO ---
        resultado.append(String.format("\n%1$s:\n", lblCuerpo));

        if (this.cuerpo != null) {
            for (Sentencia s : this.cuerpo) {
                if (s != null) {
                    resultado.append(s.generarCodigo());
                }
            }
        }

        // Regresar al bloque de evaluación
        resultado.append(String.format("br label %%%1$s\n", lblCondicion));

        // SOPORTE PARA BREAK Y CONTINUE: Desapilamos
        CodeGeneratorHelper.popContinueTag();
        CodeGeneratorHelper.popBreakTag();

        // --- BLOQUE 3: ENTORNO DEL ALT_WHILE ---
        resultado.append(String.format("\n%1$s:\n", lblEvaluarAlt));
        if (this.alternativa != null) {
            // GENERACIÓN CRÍTICA: Ahora sí inyectamos el código del alt_while si existe
            resultado.append(this.alternativa.generarCodigo(lblCondicion));  // se agrega lblCondicion
        }
        // Si no hay alternativa, o si ya pasó por ella, va linealmente al fin
        resultado.append(String.format("  br label %%%1$s\n", lblFin));

        // --- BLOQUE 4: SALIDA DEL CICLO ---
        resultado.append(String.format("\n%1$s:\n", lblFin));

        return resultado.toString();
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        dot.append(super.graficar(idPadre));
        if (condicion != null) {
            dot.append(condicion.graficar(miId));
        }

        if (cuerpo != null && !cuerpo.isEmpty()) {
            String idBody = "body_" + miId;
            dot.append(String.format("%s [label=\"BODY\"];\n", idBody));
            dot.append(String.format("%s -> %s;\n", miId, idBody));

            for (Sentencia s : cuerpo) {
                if (s != null) {
                    dot.append(s.graficar(idBody));
                }
            }
        }

        if (alternativa != null) {
            dot.append(alternativa.graficar(miId));
        }

        return dot.toString();
    }
}