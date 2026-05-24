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

        // 1. Mapeamos tu tipo de dato al tipo de LLVM y definimos su valor por defecto
        String tipoLLVM = "";
        String valorDefecto = "";

        if ("INT".equals(this.tipo)) {
            tipoLLVM = "i32";
            valorDefecto = "0";
        } else if ("FLOAT".equals(this.tipo)) {
            tipoLLVM = "float";
            valorDefecto = "0.0"; // <-- Aquí forzamos que sea 0.0 en lugar de null
        } else if ("BOOLEAN".equals(this.tipo)) {
            tipoLLVM = "i1";
            valorDefecto = "false";
        } else {
            return "; ERROR: Tipo de dato '" + this.tipo + "' no soportado en LLVM\n";
        }

        // 2. Generamos un 'alloca' seguido de un 'store' de inicialización por seguridad
        for (String var : variables) {
            String nombreVar = var.startsWith("%") ? var : "%" + var;

            // Reservamos espacio en memoria
            resultado.append(String.format("  %1$s = alloca %2$s\n", nombreVar, tipoLLVM));

            // Inicializamos inmediatamente para evitar valores 'null' colgantes en el flujo
            resultado.append(String.format("  store %1$s %2$s, %1$s* %3$s\n", tipoLLVM, valorDefecto, nombreVar));
        }

        return resultado.toString();
    }
}