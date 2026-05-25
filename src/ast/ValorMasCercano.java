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
    private final String punteroResultado; // 🌟 NUEVO: Guarda el nombre único del puntero asignado en CUP (ej: ptro.12)
    private final String tipo;

    // 🌟 CORRECCIÓN: Agregamos 'punteroResultado' al constructor
    public ValorMasCercano(Expresion referencia, String nombreArreglo, ArrayList<Declaracion> declaraciones, ArrayList<Sentencia> pasos, String punteroResultado) {
        this.referencia = referencia;
        this.nombreArreglo = nombreArreglo;
        this.declaraciones = declaraciones;
        this.pasos = pasos;
        this.punteroResultado = punteroResultado;
        this.tipo = "float"; // Mapeado internamente a double (64 bits) en tu switch backend
    }

    @Override
    protected String getEtiqueta() {
        // 🌟 Actualizado para reflejar que es un subprograma con entorno e identidad aislada
        return "SUBPROGRAMA: ValorMasCercano\\nArreglo Anónimo: " + nombreArreglo + "\\nResultado en: %" + punteroResultado;
    }

    @Override
    public String graficar(String idPadre) {
        final String miId = this.getId();
        StringBuilder sb = new StringBuilder();

        // 1. Grafica el nodo de este subprograma y lo cuelga de su padre en el AST principal
        sb.append(super.graficar(idPadre));

        // 2. Grafica de forma recursiva la expresión externa que sirve como referencia
        if (this.referencia != null) {
            sb.append(this.referencia.graficar(miId));
        }

        // 3. Grafica el arreglo anónimo y las variables locales como nodos hijos legítimos
        if (this.declaraciones != null) {
            for (Declaracion decl : declaraciones) {
                if (decl != null) {
                    sb.append(decl.graficar(miId)); // Pasa 'miId' para tirar las flechas de Graphviz desde aquí
                }
            }
        }

        // 4. Grafica de forma secuencial todo el bloque lógico de control (Asignaciones, While, Ifs)
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
        // 🌟 CORRECCIÓN CLAVE: En lugar del string fijo "%masCercano", cargamos desde el puntero dinámico único
        sb.append(String.format("%1$s = load double, double* %2$s\n",
                this.getIr_ref(),
                "%"+this.punteroResultado
        ));

        sb.append("  ; --- FIN SUBPROGRAMA ISOLADO ---\n\n");

        return sb.toString();
    }
}