package ast.literal;

import ast.Expresion;

public class IntLiteral extends Expresion {
    private final String valor;

    public IntLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    protected String getEtiqueta() {
        return "INT: " + valor;
    }

    // Mantenemos este para tu lógica semántica (operaciones, tablas, etc.)
    public String getValor() {
        return valor;
    }

    @Override
    public String graficar(String idPadre) {
        // 1. Creamos un ID visual que sea ÚNICO para cada objeto 'new IntLiteral'
        // Esto evita que Graphviz junte dos nodos que tengan el mismo valor.
        String idVisual = "int_" + System.identityHashCode(this);

        StringBuilder grafico = new StringBuilder();

        // 2. Definimos el nodo con el ID visual único
        grafico.append(String.format("%1$s[label=\"%2$s\"]\n", idVisual, this.getEtiqueta()));

        // 3. Creamos la conexión desde el padre hacia este ID visual único
        if (idPadre != null) {
            grafico.append(String.format("%1$s->%2$s\n", idPadre, idVisual));
        }

        return grafico.toString();
    }
}