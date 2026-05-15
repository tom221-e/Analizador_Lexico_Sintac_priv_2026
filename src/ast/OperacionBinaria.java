package ast;

import llvm.*;

public abstract class OperacionBinaria extends Expresion {
    protected final Expresion izquierda;
    protected final Expresion derecha;

    public OperacionBinaria(Expresion izquierda, Expresion derecha) {
        this.izquierda = izquierda;
        this.derecha = derecha;
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
    public String generarCodigo(){
        StringBuilder resultado = new StringBuilder();
        resultado.append(this.getE1().generarCodigo());
        resultado.append(this.getE2().generarCodigo());
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        resultado.append(String.format("%1$s = %2$s i32 %3$s, %4$s\n", this.getIr_ref(),
                this.get_llvm_op_code(), this.getE1().getIr_ref(),
                this.getE2().getIr_ref()));
        return resultado.toString();
    }
}