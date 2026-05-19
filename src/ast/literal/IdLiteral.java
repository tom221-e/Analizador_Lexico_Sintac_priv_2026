package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;
import parser.SymbolTable;
import validator.ValidatorDataType; // Importamos tu validador

public class IdLiteral extends Expresion {
    private final String valor;
    private final String tipo;

    public IdLiteral(String valor, String tipo) {
        this.valor = String.valueOf(valor);
        this.tipo = tipo;
    }

    public String getNombreVariable() {
        return valor;
    }

    @Override
    protected String getEtiqueta() {
        return "ID: " + valor;
    }

    public String getStringID() {
        return "%" + valor;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        // Ejemplo: %1 = load i32, i32* %x
        resultado.append(String.format("%1$s = load %2$s, %2$s* %3$s\n",
                this.getIr_ref(),   // El nuevo temporal (%1)
                this.tipo,           // El tipo (i32, float, i1)
                this.getStringID()  // El nombre de la variable original (%x)
        ));

        return resultado.toString();
    }
}

}