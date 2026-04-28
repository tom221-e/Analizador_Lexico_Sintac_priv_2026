package ast.unario;

import ast.Expresion;
import ast.OperacionUnaria;

public class MenosUnario extends OperacionUnaria {
    public MenosUnario(Expresion operando) {
        super(operando);
    }

    @Override
    protected String getNombreOperacion() {
        return "- (unario)";
    }
}