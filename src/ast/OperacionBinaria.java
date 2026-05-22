package ast;

import llvm.*;
import parser.SymbolTable;
import validator.ValidatorDataType;

public abstract class OperacionBinaria extends Expresion {
    protected final Expresion izquierda;
    protected final Expresion derecha;
    protected final String tipo;


    public OperacionBinaria(Expresion izquierda, Expresion derecha, String tipo) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.tipo = tipo;
    }

    @Override
    protected String getEtiqueta() {
        return String.format("%s", this.getNombreOperacion());
    }

    public Expresion getE1() {
        return izquierda;
    }
    public Expresion getE2() {
        return derecha;
    }

    protected abstract String getNombreOperacion();

    @Override
    protected String graficar(String idPadre) {
        final String miId = this.getId();
        return super.graficar(idPadre) +
                izquierda.graficar(miId) +
                derecha.graficar(miId);
    }
    public abstract String get_llvm_op_code();

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código de los hijos (recursividad)
        resultado.append(this.izquierda.generarCodigo());
        resultado.append(this.derecha.generarCodigo());
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        // 4. Generamos la instrucción con el tipo que nos dio el validador
        resultado.append(String.format("%1$s = %2$s %3$s %4$s, %5$s\n",
                this.getIr_ref(),           // %res
                this.get_llvm_op_code(),    // add, fadd, etc.
//                this.tipo,                   // i32, float, i1
                this.izquierda.getIr_ref(), // %op1
                this.derecha.getIr_ref()    // %op2
        ));

        return resultado.toString();
    }
}