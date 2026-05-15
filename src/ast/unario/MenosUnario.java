package ast.unario;

import ast.Expresion;
import ast.OperacionUnaria;
import ast.literal.IntLiteral;
import llvm.CodeGeneratorHelper;

public class MenosUnario extends OperacionUnaria {
    public MenosUnario(Expresion operando) {
        super(operando);
    }

    @Override
    protected String getNombreOperacion() {
        return "- (unario)";
    }
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        resultado.append(this.operando.generarCodigo());
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        if (this.operando instanceof IntLiteral) {
            // %res = sub i32 0, %valor
            resultado.append(String.format("%1$s = sub i32 0, %2$s\n",
                    this.getIr_ref(), this.operando.getIr_ref()));
        } else {
            // %res = fsub float -0.0, %valor (o 0.0)
            resultado.append(String.format("%1$s = fsub float 0.0, %2$s\n",
                    this.getIr_ref(), this.operando.getIr_ref()));
        }
        return resultado.toString();
    }
}