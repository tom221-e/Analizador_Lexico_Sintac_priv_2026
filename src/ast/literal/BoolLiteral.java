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
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // Usamos la instrucción lógica 'or' que sí es válida para i1 en LLVM
        if (this.getBool()){
            resultado.append(String.format("  %1$s = or i1 false, true\n", this.getIr_ref()));
        } else {
            resultado.append(String.format("  %1$s = or i1 false, false\n", this.getIr_ref()));
        }

        return resultado.toString();
    }
}