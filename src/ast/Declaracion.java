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
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Mapeamos tu tipo de dato al tipo de LLVM
        String tipoLLVM = "";
        if ("INT".equals(this.tipo)) {
            tipoLLVM = "i32";
        } else if ("FLOAT".equals(this.tipo)) {
            tipoLLVM = "float";
        } else if ("BOOLEAN".equals(this.tipo)) {
            tipoLLVM = "i1";
        } else {
            return "; ERROR: Tipo de dato '" + this.tipo + "' no soportado en LLVM\n";
        }

        // 2. Generamos un 'alloca' para cada variable en la lista
        for (String var : variables) {
            // En LLVM: %x = alloca i32
            // Usamos % delante del nombre de la variable para identificar el puntero local
            resultado.append(String.format("%1$s = alloca %2$s\n",
                    var,       // Nombre de la variable
                    tipoLLVM   // Tipo (i32, float, i1)
            ));
        }

        return resultado.toString();
    }
}