package ast;

import java.util.ArrayList;
import llvm.*;


public class SentenciaWhile extends Sentencia {
    private final Expresion condicion;
    private final ArrayList<Sentencia> cuerpo; // Cambiado de Bloque a ArrayList
    private final SentenciaAlt alternativa;

    public SentenciaWhile(Expresion condicion, ArrayList<Sentencia> cuerpo, SentenciaAlt alternativa) {
        this.condicion = condicion;
        this.cuerpo = cuerpo;
        this.alternativa = alternativa;
    }

    @Override
    protected String getNombreSentencia() {
        return "WHILE";
    }
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Definimos las etiquetas necesarias para el ciclo
        String lblCondicion = CodeGeneratorHelper.getNewPointer(); // Donde se evalúa si entra al loop
        String lblCuerpo = CodeGeneratorHelper.getNewPointer();   // El bloque de instrucciones
        String lblFin = CodeGeneratorHelper.getNewPointer();     // Salida del loop

        // 2. Salto inicial para entrar a la evaluación de la condición
        resultado.append(String.format("br label %1$s\n", lblCondicion));

        // --- BLOQUE 1: EVALUACIÓN DE LA CONDICIÓN ---
        resultado.append(String.format("\n%1$s:\n", lblCondicion));
        // Generamos el código de la expresión (ej: i < 10)
        resultado.append(this.condicion.generarCodigo());

        // Saltamos al cuerpo si es verdadero (i1 1) o al fin si es falso (i1 0)
        resultado.append(String.format("br i1 %1$s, label %2$s, label %3$s\n",
                this.condicion.getIr_ref(), lblCuerpo, lblFin));

        // --- BLOQUE 2: CUERPO DEL CICLO ---
        resultado.append(String.format("\n%1$s:\n", lblCuerpo));

        if (this.cuerpo != null) {
            // Ejecutamos todas las sentencias del ArrayList
            for (Sentencia s : this.cuerpo) {
                if (s != null) {
                    resultado.append(s.generarCodigo());
                }
            }
        }

        // CRUCIAL: Al terminar el cuerpo, saltamos de regreso a la condición
        resultado.append(String.format("br label %1$s\n", lblCondicion));

        // --- BLOQUE 3: SALIDA DEL CICLO ---
        resultado.append(String.format("\n%1$s:\n", lblFin));

        return resultado.toString();
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // 1. Graficar el nodo principal WHILE y su condición
        dot.append(super.graficar(idPadre));
        if (condicion != null) {
            dot.append(condicion.graficar(miId));
        }

        // 2. Crear el nodo intermedio "BODY" para las sentencias del cuerpo
        if (cuerpo != null && !cuerpo.isEmpty()) {
            String idBody = "body_" + miId;

            // Creamos el globo visual "BODY"
            dot.append(String.format("%s [label=\"BODY\"];\n", idBody));
            dot.append(String.format("%s -> %s;\n", miId, idBody));

            // Graficamos cada sentencia de la lista dentro del nodo BODY
            for (Sentencia s : cuerpo) {
                if (s != null) {
                    dot.append(s.graficar(idBody));
                }
            }
        }

        // 3. Graficar la alternativa (si existe)
        if (alternativa != null) {
            dot.append(alternativa.graficar(miId));
        }

        return dot.toString();
    }
}