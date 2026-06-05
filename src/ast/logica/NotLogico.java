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
        
        // 1. generar código del operando
        resultado.append(this.operando.generarCodigo());
        
        // 2. nuevo registro para el resultado
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 3. Aplicamos la lógica XOR con el valor 1 (true)
        // Sintaxis: %res = xor i1 %valor, 1
        //resultado.append(String.format("%1$s = xor i1 %2$s, true\n", se quita
                //this.getIr_ref(), this.operando.getIr_ref()));
        
        // 3. XOR con true invierte el bit booleano
        resultado.append(String.format("  %1$s = xor i1 %2$s, true\n",  // ← dos espacios
                this.getIr_ref(), this.operando.getIr_ref()));

        return resultado.toString();
    }
}