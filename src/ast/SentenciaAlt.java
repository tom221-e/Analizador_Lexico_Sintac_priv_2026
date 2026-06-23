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
        // sin lblCondicionWhile no se puede corregir la semántica
        // este método solo se llama si no hay WHILE padre — no debería ocurrir
        return generarCodigo(null);
    }

    public String generarCodigo(String lblCondicionWhile) {
        StringBuilder resultado = new StringBuilder();

        String lblCondicion = CodeGeneratorHelper.getNewTag();
        String lblCuerpo    = CodeGeneratorHelper.getNewTag();
        String lblSiguiente = CodeGeneratorHelper.getNewTag();

        resultado.append(String.format("  br label %%%s\n", lblCondicion));

        // condición del ALT
        resultado.append(String.format("\n%s:\n", lblCondicion));
        resultado.append(this.condicion.generarCodigo());
        resultado.append(String.format("  br i1 %1$s, label %%%2$s, label %%%3$s\n",
                this.condicion.getIr_ref(), lblCuerpo, lblSiguiente));

        // cuerpo del ALT — al terminar vuelve al WHILE
        resultado.append(String.format("\n%s:\n", lblCuerpo));
        if (this.cuerpo != null) {
            for (Sentencia s : this.cuerpo) {
                if (s != null) resultado.append(s.generarCodigo());
            }
        }
        // ← vuelve a la condición del WHILE, no al fin local
        if (lblCondicionWhile != null) {
            resultado.append(String.format("  br label %%%s\n", lblCondicionWhile));
        }

        // siguiente ALT o cae al fin del WHILE
        resultado.append(String.format("\n%s:\n", lblSiguiente));
        if (this.alternativa != null) {
            resultado.append(this.alternativa.generarCodigo(lblCondicionWhile));
        }
        // no emite br acá — el WHILE emite su propio br label %lblFin

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
            String idBody = "body_alt_" + miId;
            dot.append(String.format("%s [label=\"BODY\"];\n", idBody));
            dot.append(String.format("%s -> %s;\n", miId, idBody));
            for (Sentencia s : cuerpo) {
                if (s != null) dot.append(s.graficar(idBody));
            }
        }

        if (alternativa != null) {
            dot.append(alternativa.graficar(miId));
        }

        return dot.toString();
    }

}