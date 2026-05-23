package ast;

import java.util.ArrayList;

public class ValorMasCercano extends Expresion {
    private final Expresion referencia;
    private final Object lista; // Cambiado de String a Object o el tipo de FLOAT_ARRAY
    private final ArrayList<Sentencia> pasos;

    public ValorMasCercano(Expresion referencia, Object lista, ArrayList<Sentencia> pasos) {
        this.referencia = referencia;
        this.lista = lista;
        this.pasos = pasos;
    }

    @Override
    protected String getEtiqueta() {
        // Evita usar el objeto directamente aquí si puede contener comas
        return "VALOR_MAS_CERCANO";
    }

    @Override
    public String generarCodigo() {
        return "";
    }

    @Override
    public String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        dot.append(super.graficar(idPadre));

        // Graficar la lista (solo si es un Nodo del AST)
        if (lista instanceof Nodo) {
            dot.append(((Nodo) lista).graficar(miId));
        }

        // Graficar los pasos expandidos
        for (Sentencia s : pasos) {
            if (s != null) dot.append(s.graficar(miId));
        }

        return dot.toString();
    }
}