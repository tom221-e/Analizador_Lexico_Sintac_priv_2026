package ast;

import java.util.ArrayList;
import llvm.CodeGeneratorHelper;

public class SentenciaIf extends Sentencia {
    private final Expresion condicion;
    private final ArrayList<Sentencia> cuerpoThen;
    private final ArrayList<Sentencia> sentenciaElse;

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

        dot.append(super.graficar(idPadre));
        dot.append(condicion.graficar(miId));

        if (cuerpoThen != null && !cuerpoThen.isEmpty()) {
            String idThen = "then_" + miId;
            dot.append(String.format("%s [label=\"THEN\"];\n", idThen));
            dot.append(String.format("%s -> %s;\n", miId, idThen));
            for (Sentencia s : cuerpoThen) {
                if (s != null) dot.append(s.graficar(idThen));
            }
        }

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

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        String labelThen = CodeGeneratorHelper.getNewTag();
        String labelElse = CodeGeneratorHelper.getNewTag();
        String labelEnd  = CodeGeneratorHelper.getNewTag();

        // Evaluamos la expresión condicional
        resultado.append(this.condicion.generarCodigo());

        String destinoElse = (sentenciaElse != null && !sentenciaElse.isEmpty()) ? labelElse : labelEnd;

        // CORRECCIÓN: Añadido '%%' antes de %2$s y %3$s para generar el '%' requerido por LLVM
        resultado.append(String.format("br i1 %1$s, label %%%2$s, label %%%3$s\n\n",
                this.condicion.getIr_ref(),
                labelThen,
                destinoElse
        ));

        // ==========================================
        // BLOQUE THEN
        // ==========================================
        resultado.append(labelThen).append(":\n");
        if (cuerpoThen != null) {
            for (Sentencia s : cuerpoThen) {
                if (s != null) {
                    resultado.append(s.generarCodigo());
                }
            }
        }
        // CORRECCIÓN: Añadido '%%' para el salto incondicional (br label %tag.X)
        resultado.append(String.format("br label %%%1$s\n\n", labelEnd));

        // ==========================================
        // BLOQUE ELSE
        // ==========================================
        if (sentenciaElse != null && !sentenciaElse.isEmpty()) {
            resultado.append(labelElse).append(":\n");
            for (Sentencia s : sentenciaElse) {
                if (s != null) {
                    resultado.append(s.generarCodigo());
                }
            }
            // CORRECCIÓN: Añadido '%%' para el salto incondicional
            resultado.append(String.format("br label %%%1$s\n\n", labelEnd));
        }

        // ==========================================
        // ETIQUETA DE SALIDA
        // ==========================================
        resultado.append(labelEnd).append(":\n");

        return resultado.toString();
    }
}