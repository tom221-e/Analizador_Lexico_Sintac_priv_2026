package ast.logica;

import ast.Expresion;
import ast.OperacionUnaria;
import llvm.*;

public class NotLogico extends OperacionUnaria {
    public NotLogico(Expresion operando) {
        super(operando);
    }

    @Override
    protected String getNombreOperacion() {
        return "NOT";
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        resultado.append(this.operando.generarCodigo());

        // 2. Pedimos un nuevo registro para el resultado de la negación
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 3. Aplicamos la lógica XOR con el valor 1 (true)
        // Sintaxis: %res = xor i1 %valor, 1
        resultado.append(String.format("%1$s = xor i1 %2$s, true\n",
                this.getIr_ref(), this.operando.getIr_ref()));

        return resultado.toString();
    }
}