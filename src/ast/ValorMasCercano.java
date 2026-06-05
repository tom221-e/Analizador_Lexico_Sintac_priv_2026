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
    private final String punteroResultado;
    private final String tipo;
    private final String valoresArregloString; // <--- Nuevo campo

    public ValorMasCercano(Expresion referencia, String nombreArreglo, ArrayList<Declaracion> declaraciones, ArrayList<Sentencia> pasos, String punteroResultado, String valoresArregloString) {
        this.referencia = referencia;
        this.nombreArreglo = nombreArreglo;
        this.declaraciones = declaraciones;
        this.valoresArregloString = valoresArregloString;
        this.pasos = pasos;
        this.punteroResultado = punteroResultado;
        this.tipo = "float";
    }

    @Override
    protected String getEtiqueta() {
        // Aquí construimos el texto exacto que quieres que aparezca
        // Asumimos que referencia.toString() devuelve "5" o el valor correspondiente
        return "SUBPROGRAMA: valor_mas_cercano(" + referencia.getValor() + ", " + valoresArregloString + ")";
    }

    @Override
    public String graficar(String idPadre) {
        final String miId = this.getId();
        StringBuilder sb = new StringBuilder();

        sb.append(super.graficar(idPadre));

        if (this.referencia != null) {
            sb.append(this.referencia.graficar(miId));
        }

        if (this.declaraciones != null) {
            for (Declaracion decl : declaraciones) {
                if (decl != null) {
                    sb.append(decl.graficar(miId));
                }
            }
        }

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

        sb.append("\n  ; --- INICIO SUBPROGRAMA ISOLADO (ARREGLO ANÓNIMO) ---\n");

        // 2. Genera las instrucciones de todos los pasos sintácticos de la macro
        for (Sentencia paso : pasos) {
            if (paso != null) {
                sb.append(paso.generarCodigo());
            }
        }

        // =========================================================================
        // RETORNO CON PUNTERO DINÁMICO ÚNICO
        // =========================================================================
        // Generamos un registro SSA nuevo para el retorno limpio del valor de la expresión
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        sb.append("  ; --- EXTRAER RESULTADO DESDE REGISTRO DINÁMICO ---\n");

        // 🌟 CORRECCIÓN CRÍTICA: Cambiado 'double*' por 'ptr' para mantener la consistencia del backend moderno
        sb.append(String.format("  %1$s = load double, ptr %2$s\n",
                this.getIr_ref(),
                "%" + this.punteroResultado
        ));

        sb.append("  ; --- FIN SUBPROGRAMA ISOLADO ---\n\n");

        return sb.toString();
    }
}