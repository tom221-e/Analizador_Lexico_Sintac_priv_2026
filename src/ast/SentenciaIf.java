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
        return generarCodigo(null);
    }

    public String generarCodigo(String labelEndExterno) {
        StringBuilder resultado = new StringBuilder();

        String labelThen = CodeGeneratorHelper.getNewTag();
        String labelElse = CodeGeneratorHelper.getNewTag();
        String labelEnd  = (labelEndExterno != null) ? labelEndExterno
                                                     : CodeGeneratorHelper.getNewTag();

        resultado.append(this.condicion.generarCodigo());

        String destinoElse = (sentenciaElse != null && !sentenciaElse.isEmpty())
                             ? labelElse : labelEnd;

        resultado.append(String.format("  br i1 %1$s, label %%%2$s, label %%%3$s\n\n",
                this.condicion.getIr_ref(), labelThen, destinoElse));

        // bloque THEN
        resultado.append(labelThen).append(":\n");
        if (cuerpoThen != null) {
            for (Sentencia s : cuerpoThen) {
                if (s != null) resultado.append(s.generarCodigo());
            }
        }
        resultado.append(String.format("  br label %%%1$s\n\n", labelEnd));

        // bloque ELSE
        if (sentenciaElse != null && !sentenciaElse.isEmpty()) {
            resultado.append(labelElse).append(":\n");
            for (Sentencia s : sentenciaElse) {
                if (s != null) {
                    if (s instanceof SentenciaIf) {
                        // ELIF: le pasa el labelEnd para compartir etiqueta de salida
                        resultado.append(((SentenciaIf) s).generarCodigo(labelEnd));
                    } else {
                        resultado.append(s.generarCodigo());
                    }
                }
            }
            resultado.append(String.format("  br label %%%1$s\n\n", labelEnd));
        }

        // etiqueta de salida — solo si es propia (no heredada)
        if (labelEndExterno == null) {
            resultado.append(labelEnd).append(":\n");
        }

        return resultado.toString();
    }

}