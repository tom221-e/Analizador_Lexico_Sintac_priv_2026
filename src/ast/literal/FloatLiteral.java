package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class FloatLiteral extends Expresion {
    private final String valor;

    public FloatLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "FLOAT: " + valor; // Etiqueta para el globo del grafo
    }
    public Float getFloat() {
        return Float.parseFloat(valor);
    }
    
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        resultado.append(String.format("  %1$s = fadd double 0.0, %2$s\n", this.getIr_ref(), getFloat()));
        return resultado.toString();
    }
    @Override
    public Expresion copiar() {
        return new FloatLiteral(this.valor);
    }
    @Override
    public String getTipo() {
        return "FLOAT";
    }
}