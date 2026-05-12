package ast;

import ast.literal.IdLiteral;

public class Asignacion extends Sentencia {
    private final String id;
    private final Expresion valor;

    public Asignacion(String id, Expresion valor) {
        this.id = id;
        this.valor = valor;
    }

    @Override
    protected String getNombreSentencia() {
        return "="; // El globo del grafo mostrará el nombre de la variable
    }

    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder grafico = new StringBuilder();

        // 1. Graficamos el nodo actual (el "=")
        grafico.append(super.graficar(idPadre));

        // 2. Graficamos el lado IZQUIERDO (el ID)
        // Creamos un IdLiteral temporal para que genere su propio globo
        IdLiteral nodoId = new IdLiteral(id);
        grafico.append(nodoId.graficar(miId));

        // 3. Graficamos el lado DERECHO (el valor o expresión)
        grafico.append(valor.graficar(miId));

        return grafico.toString();
    }
}