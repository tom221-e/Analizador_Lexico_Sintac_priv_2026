package ast;

import ast.Expresion;
import ast.Sentencia;
import ast.Declaracion;
import java.util.ArrayList;
import llvm.CodeGeneratorHelper;

public class ValorMasCercano extends Expresion {
    private final Expresion referencia;
    private final String nombreArreglo;
    private final ArrayList<Declaracion> declaraciones;
    private final ArrayList<Sentencia> pasos;
    private final String tipo;

    public ValorMasCercano(Expresion referencia, String nombreArreglo, ArrayList<Declaracion> declaraciones, ArrayList<Sentencia> pasos) {
        this.referencia = referencia;
        this.nombreArreglo = nombreArreglo;
        this.declaraciones = declaraciones;
        this.pasos = pasos;
        this.tipo = "float"; // Aseguramos el tipo nativo para el auto-casting
    }

    @Override
    protected String getEtiqueta() {
        // Formato visible en el nodo burbuja de Graphviz
        return "MACRO: ValorMasCercano\\nArreglo: " + nombreArreglo;
    }

    @Override
    protected String graficar(String idPadre) {
        final String miId = this.getId();
        StringBuilder sb = new StringBuilder();

        // 1. Grafica el nodo actual y lo enlaza con su padre en el AST
        sb.append(super.graficar(idPadre));

        // 2. Grafica de forma recursiva el nodo de la expresión de referencia (ej: el número 5)
        if (this.referencia != null) {
            sb.append(this.referencia.graficar(miId));
        }

        // 3. Recorre y concatena los sub-árboles de todos los pasos sintácticos que ejecuta la macro
        if (this.pasos != null) {
            for (Sentencia paso : pasos) {
                if (paso != null) {
                    sb.append(paso.graficar(miId));
                }
            }
        }

        return sb.toString();
    }

    @Override
    public String generarCodigo() {
        StringBuilder sb = new StringBuilder();

        sb.append("; --- INICIO MACRO VALOR_MAS_CERCANO ---\n");

        // 1. Ejecutamos todos los pasos internos de la macro (inicialización, while, etc.)
        for (Sentencia paso : pasos) {
            if (paso != null) {
                sb.append(paso.generarCodigo());
            }
        }

        // =========================================================================
        // RETORNO DE VARIABLE: Extraemos el resultado de %masCercano a un registro nuevo
        // =========================================================================
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        sb.append("; --- RETORNO DE MACRO A ASIGNACIÓN ---\n");
        sb.append(String.format("  %1$s = load float, float* %%masCercano\n", this.getIr_ref()));

        sb.append("; --- FIN MACRO VALOR_MAS_CERCANO ---\n");

        return sb.toString();
    }
}