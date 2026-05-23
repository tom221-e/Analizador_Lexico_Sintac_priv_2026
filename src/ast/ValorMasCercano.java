package ast;

import java.util.ArrayList;

public class ValorMasCercano extends Expresion {
    private Expresion ref;
    private String listaId;
    private ArrayList<Declaracion> declaracionesTemporales;
    private ArrayList<Sentencia> pasosCuerpo;

    public ValorMasCercano(Expresion ref, String listaId, ArrayList<Declaracion> declaracionesTemporales, ArrayList<Sentencia> pasosCuerpo) {
        this.ref = ref;
        this.listaId = listaId;
        this.declaracionesTemporales = declaracionesTemporales;
        this.pasosCuerpo = pasosCuerpo;
    }

    @Override
    protected String getEtiqueta() {
        return "VALOR_MAS_CERCANO";
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Primero generamos los 'alloca' de las variables temporales de la macro
        for (Declaracion dec : declaracionesTemporales) {
            resultado.append(dec.generarCodigo());
        }

        // 2. Luego generamos el código de la lógica (asignaciones, while, ifs)
        for (Sentencia sent : pasosCuerpo) {
            resultado.append(sent.generarCodigo());
        }

        return resultado.toString();
    }

    @Override
    public String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // 1. Grafica este nodo y lo conecta a su padre
        dot.append(super.graficar(idPadre));

        // 2. Graficar la expresión de referencia (ref) si existe
        if (ref != null) {
            dot.append(ref.graficar(miId));
        }

        // 3. Graficar el identificador del arreglo de entrada (listaId)
        if (listaId != null) {
            // ENVOLVEMOS EL ID ENTRE COMILLAS DOBLES: \"
            String idLista = "\"array_" + listaId + "_" + miId + "\"";

            dot.append(idLista).append(" [label=\"Arreglo: ").append(listaId).append("\", color=\"purple\"];\n");
            dot.append(miId).append(" -> ").append(idLista).append(";\n");
        }

        // 4. Graficar los nodos de las declaraciones temporales internas
        if (declaracionesTemporales != null) {
            for (Declaracion dec : declaracionesTemporales) {
                if (dec != null) {
                    dot.append(dec.graficar(miId));
                }
            }
        }

        // 5. Graficar los pasos expandidos de la lógica (Asignaciones, Whiles, Ifs)
        if (pasosCuerpo != null) {
            for (Sentencia s : pasosCuerpo) {
                if (s != null) {
                    dot.append(s.graficar(miId));
                }
            }
        }

        return dot.toString();
    }
}