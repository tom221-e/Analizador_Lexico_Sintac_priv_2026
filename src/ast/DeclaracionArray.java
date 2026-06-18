package ast;
import java.util.ArrayList;
import llvm.CodeGeneratorHelper;

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
        StringBuilder dot = new StringBuilder();

        // 1. Dibujamos el nodo propio de la DeclaracionArray
        dot.append(miId).append(" [label=\"").append(this.getEtiqueta()).append("\", color=\"black\"];\n");

        // 2. Nos conectamos con nuestro padre en el AST (el Programa o la Macro)
        if (idPadre != null) {
            dot.append(idPadre).append(" -> ").append(miId).append(";\n");
        }

        // 3. Dibujamos nuestros hijos (las variables) una Sola Vez y en azul
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
            resultado.append(String.format("  %1$s = alloca [%2$s x double]\n", 
                    "%" + var, 
                    this.tamano
            ));
            
            // INICIALIZACIÓN DE ARRAY EN 0
            // 1. obtener puntero al primer elemento
            String ptrElem = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format(
                "%s = getelementptr [%s x double], ptr %%%s, i64 0, i64 0\n",
                ptrElem, this.tamano, var
            ));

            // 2. calcular el tamaño en bytes: N elementos * 8 bytes por double
            int byteSize = Integer.parseInt(this.tamano) * 8;

            // 3. llamar a memset con 0
            resultado.append(String.format(
                "call void @llvm.memset.p0.i64(ptr %s, i8 0, i64 %d, i1 false)\n",
                ptrElem, byteSize
            ));
        }
        return resultado.toString();
    }
}