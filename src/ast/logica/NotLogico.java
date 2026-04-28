package ast.logica;

import ast.Expresion;
import ast.OperacionUnaria;

public class NotLogico extends OperacionUnaria {
    public NotLogico(Expresion operando) {
        super(operando);
    }

    @Override
    protected String getNombreOperacion() {
        return "NOT";
    }
}