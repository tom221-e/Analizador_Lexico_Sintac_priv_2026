package ast;
import java.util.ArrayList;

// 1. Cambiamos 'extends Nodo' por 'extends Declaracion'
public class DeclaracionArray extends Declaracion {
    private final String tamano;
    private final ArrayList<String> variables;

    public DeclaracionArray(String tamano, ArrayList<String> variables) {
        // 2. LLAMADA OBLIGATORIA AL PADRE (super):
        // Como 'Declaracion' pide (tipo, variables) en su constructor,
        // le pasamos "FLOAT_ARRAY" como tipo y la lista de variables.
        super("FLOAT_ARRAY", variables);

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
        StringBuilder dot = new StringBuilder(super.graficar(idPadre));

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

    @Override
    public ArrayList<String> getVariables() {
        return variables;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        for (String var : variables) {
            // Genera directamente:  %nombreVariable = alloca [tamaño x float]
            // Agregamos el '%' antes del nombre para mantener consistencia con LLVM
            resultado.append(String.format("%1$s = alloca [ %2$s x float]\n",
                    "%" + var,
                    this.tamano
            ));
        }
        return resultado.toString();
    }
}