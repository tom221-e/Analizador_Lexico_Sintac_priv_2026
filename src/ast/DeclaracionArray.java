package ast;
import java.util.ArrayList;

public class DeclaracionArray extends Nodo {
    private final String tamano;
    private final ArrayList<String> variables;

    public DeclaracionArray(String tamano, ArrayList<String> variables) {
        this.tamano = tamano;
        this.variables = variables;
    }

    @Override
    protected String getEtiqueta() {
        return "DECLARACION: Array[" + tamano + "]";
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

    public String getTamano() {
        return tamano;
    }

    public ArrayList<String> getVariables() {
        return variables;
    }
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // Como el array es específico para FLOAT, hardcodeamos el tipo en LLVM
        for (String var : variables) {
            // Genera directamente:  %nombreVariable = alloca [tamaño x float]
            resultado.append(String.format("%1$s = alloca [ %2$s x float]\n",
                    var,
                    this.tamano
            ));
        }
        return resultado.toString();
    }
}