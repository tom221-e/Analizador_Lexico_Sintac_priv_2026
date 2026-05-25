package llvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CodeGeneratorHelper {

    private static int nextID = 0;
    private static final Stack<String> breakStack = new Stack<>();
    private static final Stack<String> continueStack = new Stack<>();
    private static List<String> constantesGlobales = new ArrayList<>();

    private CodeGeneratorHelper(){}

    /**
     * Revisa los tipos de los operandos. Si la operación exige un 'double' pero
     * recibe un 'i32', genera la instrucción 'sitofp' para castearlo y balancear la balanza.
     *
     * @param izq Nodo izquierdo
     * @param der Nodo derecho
     * @param tipoEmision El tipo final que exige la operación (ej: "double" o "i32")
     * @param sb El StringBuilder actual para inyectar la instrucción de casteo si hace falta
     * @return Arreglo con los 2 punteros definitivos [punteroIzq, punteroDer] listos para usarse.
     */
    public static String[] balancearTiposLLVM(ast.Expresion izq, ast.Expresion der, String tipoEmision, StringBuilder sb) {
        String ptrIzq = izq.getIr_ref().trim();
        String ptrDer = der.getIr_ref().trim();

        // Obtenemos los tipos originales de los nodos (asumiendo que Expresion tiene getTipo())
        String tipoIzq = (izq.getTipo() != null) ? izq.getTipo() : "i32";
        String tipoDer = (der.getTipo() != null) ? der.getTipo() : "i32";

        // Si la instrucción LLVM exige double, promovemos los enteros
        if (tipoEmision.equals("double")) {

            if (tipoIzq.equals("i32") || tipoIzq.equals("int")) {
                String nuevoPtrIzq = CodeGeneratorHelper.getNewPointer();
                sb.append(String.format("  %1$s = sitofp i32 %2$s to double\n", nuevoPtrIzq, ptrIzq));
                ptrIzq = nuevoPtrIzq; // Actualizamos el puntero al casteado
            }

            if (tipoDer.equals("i32") || tipoDer.equals("int")) {
                String nuevoPtrDer = CodeGeneratorHelper.getNewPointer();
                sb.append(String.format("  %1$s = sitofp i32 %2$s to double\n", nuevoPtrDer, ptrDer));
                ptrDer = nuevoPtrDer; // Actualizamos el puntero al casteado
            }
        }

        return new String[]{ptrIzq, ptrDer};
    }
    public static String ejecutarOperacionVectorialCompleta(ast.OperacionBinaria nodo, String refIzq, String refDer, String tamanoArreglo, String codigoALU) {
        StringBuilder sb = new StringBuilder();
        String tipoEstructuraLLVM = "[" + tamanoArreglo + " x float]";

        String ptrArrayIzq = refIzq;
        String ptrArrayDer = refDer;

        // Detectamos si son escalares (si no contienen palabras clave de arreglos)
        boolean izqEsEscalar = !refIzq.contains("data") && !refIzq.contains("Array") && !refIzq.contains("result");
        boolean derEsEscalar = !refDer.contains("data") && !refDer.contains("Array") && !refDer.contains("result");

        // 1. Broadcasting Izquierdo si aplica
        if (izqEsEscalar) {
            String arrayTempIzq = CodeGeneratorHelper.getNewPointer();
            sb.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempIzq, tipoEstructuraLLVM, tamanoArreglo, refIzq, "Izquierdo"));
            ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n", ptrArrayIzq, tipoEstructuraLLVM, arrayTempIzq));
        }

        // 2. Broadcasting Derecho si aplica
        if (derEsEscalar) {
            String arrayTempDer = CodeGeneratorHelper.getNewPointer();
            sb.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempDer, tipoEstructuraLLVM, tamanoArreglo, refDer, "Derecho"));
            ptrArrayDer = CodeGeneratorHelper.getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n", ptrArrayDer, tipoEstructuraLLVM, arrayTempDer));
        }

        // 3. Generación del contenedor del resultado final
        String temporalResultado = CodeGeneratorHelper.getNewPointer();
        nodo.setIr_ref(temporalResultado); // Asignamos el puntero al nodo de la AST

        sb.append("; --- Llamada final a la función ALU de Arreglos ---\n");
        sb.append(String.format("  %1$s = alloca %2$s\n", temporalResultado, tipoEstructuraLLVM));

        String ptrResultadoPlano = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n", ptrResultadoPlano, tipoEstructuraLLVM, temporalResultado));

        // 4. Invocación matemática real a la función de C/C++
        sb.append(String.format("  call void @operar_arreglos(float* %1$s, float* %2$s, float* %3$s, i32 %4$s, i32 %5$s)\n",
                ptrArrayIzq, ptrArrayDer, ptrResultadoPlano, tamanoArreglo, codigoALU));

        return sb.toString();
    }

    /**
     * Genera el bloque LLVM intermedio para realizar el Broadcasting de un escalar a un arreglo.
     * Convierte el valor escalar de alta precisión (double) a la precisión del arreglo (float).
     *
     * @param arrayTemp Nombre del puntero temporal asignado para el arreglo (alloca)
     * @param tipoEstructura Formato de la estructura LLVM, ej: "[10 x float]"
     * @param tamano Longitud o dimensión del arreglo en formato String (ej: "10")
     * @param valRef Registro LLVM que contiene el valor escalar a replicar (ej: "%ptro.5")
     * @param lado Identificador para los comentarios del código, ej: "Izquierdo" o "Derecho"
     * @return Cadena de texto con el código LLVM estructurado en bloques de control.
     */
    public static String generarBloqueBroadcasting(String arrayTemp, String tipoEstructura, String tamano, String valRef, String lado) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("; --- Broadcasting %1$s (Conversión automática double -> float) ---\n", lado));
        sb.append(String.format("  %1$s = alloca %2$s\n", arrayTemp, tipoEstructura));

        // Asignamos y limpiamos el índice del iterador (i32)
        String idx = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = alloca i32\n", idx));
        sb.append(String.format("  store i32 0, i32* %1$s\n", idx));

        // Generamos las etiquetas de salto para el bucle
        String labelCond = CodeGeneratorHelper.getNewTag();
        String labelBody = CodeGeneratorHelper.getNewTag();
        String labelEnd  = CodeGeneratorHelper.getNewTag();

        // --- Bloque Condición ---
        sb.append("  br label %" + labelCond + "\n");
        sb.append(labelCond + ":\n");
        String tIdx = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = load i32, i32* %2$s\n", tIdx, idx));
        String cmp = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = icmp slt i32 %2$s, %3$s\n", cmp, tIdx, tamano));
        sb.append(String.format("  br i1 %1$s, label %%%2$s, label %%%3$s\n", cmp, labelBody, labelEnd));

        // --- Bloque Cuerpo ---
        sb.append(labelBody + ":\n");

        // 🌟 Conversión obligatoria de bits: Truncamos el double escalar a float para guardarlo en el vector
        String valTruncado = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = fptrunc double %2$s to float\n", valTruncado, valRef.trim()));

        // Obtenemos la posición de memoria e insertamos el elemento truncado
        String ptrPos = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 %4$s\n", ptrPos, tipoEstructura, arrayTemp, tIdx));
        sb.append(String.format("  store float %1$s, float* %2$s\n", valTruncado, ptrPos));

        // Incrementamos el iterador indexado
        String nIdx = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = add i32 %2$s, 1\n", nIdx, tIdx));
        sb.append(String.format("  store i32 %1$s, i32* %2$s\n", nIdx, idx));
        sb.append("  br label %" + labelCond + "\n");

        // --- Cierre del bucle ---
        sb.append(labelEnd + ":\n");

        return sb.toString();
    }

    public static String getNewPointer(){
        StringBuilder ret = new StringBuilder();
        nextID+=1;
        ret.append(String.format("%%ptro.%s", nextID));
        return ret.toString();
    }

    public static String getNewGlobalPointer(){
        StringBuilder ret = new StringBuilder();
        nextID+=1;
        ret.append(String.format("@gb.%s", nextID));
        return ret.toString();
    }

    public static String getNewTag(){
        StringBuilder ret = new StringBuilder();
        nextID+=1;
        ret.append(String.format("tag.%s", nextID));
        return ret.toString();
    }
    // MÉTODOS AGREGADOS PARA MANEJAR EL CONTINUE

    public static void pushBreakTag(String tag) {
        breakStack.push(tag);
    }

    public static void popBreakTag() {
        if (!breakStack.isEmpty()) {
            breakStack.pop();
        }
    }

    public static String getCurrentBreakTag() {
        return breakStack.isEmpty() ? null : breakStack.peek();
    }

    // MÉTODOS AGREGADOS PARA MANEJAR EL CONTINUE
    public static void pushContinueTag(String tag) {
        continueStack.push(tag);
    }

    public static void popContinueTag() {
        if (!continueStack.isEmpty()) {
            continueStack.pop();
        }
    }

    public static String getCurrentContinueTag() {
        return continueStack.isEmpty() ? null : continueStack.peek();
    }
    public static void agregarConstanteGlobal(String declaracion) {
        constantesGlobales.add(declaracion);
    }


    public static String obtenerConstantesGlobales() {
        StringBuilder sb = new StringBuilder();
        for (String constante : constantesGlobales) {
            sb.append(constante);
        }
        return sb.toString();
    }
}