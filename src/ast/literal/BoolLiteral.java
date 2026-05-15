package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class BoolLiteral extends Expresion {
    private final String valor;

    public BoolLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "Bool: " + valor; // Etiqueta para el globo del grafo
    }
    public Boolean getBool() {
    return Boolean.valueOf(valor);
    }
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        resultado.append(String.format("%1$s = add i1 0, %2$s\n", this.getIr_ref(), getBool()));
        return resultado.toString();
    }
}