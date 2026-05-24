package ast; // o el paquete donde lo tengas

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

    public ValorMasCercano(Expresion referencia, String nombreArreglo, ArrayList<Declaracion> declaraciones, ArrayList<Sentencia> pasos) {
        this.referencia = referencia;
        this.nombreArreglo = nombreArreglo;
        this.declaraciones = declaraciones;
        this.pasos = pasos;
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
        // SOLUCIÓN AQUÍ: Extraemos el resultado de %masCercano a un registro nuevo
        // =========================================================================
        this.setIr_ref(CodeGeneratorHelper.getNewPointer()); // Generamos el %ptro final de la expresión

        sb.append("; --- RETORNO DE MACRO A ASIGNACIÓN ---\n");
        sb.append(String.format("  %1$s = load float, float* %%masCercano\n", this.getIr_ref()));

        sb.append("; --- FIN MACRO VALOR_MAS_CERCANO ---\n");

        return sb.toString();
    }
}