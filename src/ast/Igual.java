package ast;

public class Igual extends OperacionBinaria {
    public Igual(Expresion izquierda, Expresion derecha) {
        super(izquierda, derecha);
    }

    @Override
    protected String getNombreOperacion() {
        return "="; // Corresponde a Igual
    }
}