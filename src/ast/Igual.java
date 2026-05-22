package ast;

public class Igual extends OperacionBinaria {
    public Igual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "="; // Corresponde a Igual
    }

    @Override
    public String get_llvm_op_code() {
        return("igual");
    }
}
