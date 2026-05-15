package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class IdLiteral extends Expresion {
    private final String valor;

    public IdLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    // CAMBIO AQUÍ: Renombrar para no sobreescribir al padre
    public String getNombreVariable() {
        return valor;
    }

    @Override
    protected String getEtiqueta() {
        return "ID: " + valor;
    }
    public String getStringID() {
        return valor;
    }
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        // %ptro.1 = load float, float* %nombreVariable
        resultado.append(String.format("%1$s = load float, float* %2$s\n",
                this.getIr_ref(), this.getStringID()));
        return resultado.toString();
    }

}