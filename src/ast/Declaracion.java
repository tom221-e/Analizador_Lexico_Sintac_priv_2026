package ast;
import java.util.ArrayList;

public class Declaracion extends Nodo {
    private final String tipo;
    private final ArrayList<String> variables;

    public Declaracion(String tipo, ArrayList<String> variables) {
        this.tipo = tipo;
        this.variables = variables;
    }

    @Override
    protected String getEtiqueta() {
        return "DECLARACION: " + tipo;
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        // 1. Grafica el nodo de la declaración (el "globo" con el tipo de dato)
        StringBuilder dot = new StringBuilder(super.graficar(idPadre));

        // 2. Crea y conecta nodos para cada variable declarada
        for (String var : variables) {
            String idVar = "var_" + var + "_" + miId;
            dot.append(idVar).append(" [label=\"Variable: ").append(var).append("\", color=\"blue\"];\n");
            dot.append(miId).append(" -> ").append(idVar).append(";\n");
        }

        return dot.toString();
    }

    public String getTipo() {
        return tipo;
    }

    public ArrayList<String> getVariables() {
        return variables;
    }
}