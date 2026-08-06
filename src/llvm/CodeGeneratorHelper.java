package llvm;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CodeGeneratorHelper {

    private static int nextID = 0;
    private static final Stack<String> breakStack = new Stack<>();
    private static final Stack<String> continueStack = new Stack<>();
    private static List<String> constantesGlobales = new ArrayList<>();

    public static void reset() {  // Corrección: resetea estado entre distintas compilaciones
        nextID = 0;
        breakStack.clear();
        continueStack.clear();
        constantesGlobales.clear();
    }
    private CodeGeneratorHelper(){}

    public static String[] castearOperandosAlVuelo(ast.Expresion izquierda, ast.Expresion derecha, String tipoDestinoLLVM, StringBuilder sb) {
        String ptrIzq = izquierda.getIr_ref().trim();
        String ptrDer = derecha.getIr_ref().trim();

        // Solo analizamos si la operación final de LLVM requiere alta precisión (double)
        if ("double".equals(tipoDestinoLLVM)) {

            // NUEVA VALIDACIÓN DIRECTA: Obtenemos el tipo directamente de cada nodo expresión
            String tipoIzq = izquierda.getTipo();
            String tipoDer = derecha.getTipo();

            // Si el izquierdo es INT (o su tipo LLVM i32), se castea a double
            if ("INT".equals(tipoIzq) || "i32".equals(tipoIzq)) {
                String nuevoPtr = CodeGeneratorHelper.getNewPointer();
                sb.append(String.format("  %1$s = sitofp i32 %2$s to double\n", nuevoPtr, ptrIzq));
                ptrIzq = nuevoPtr;
            }

            // Si el derecho es INT (o su tipo LLVM i32), se castea a double
            if ("INT".equals(tipoDer) || "i32".equals(tipoDer)) {
                String nuevoPtr = CodeGeneratorHelper.getNewPointer();
                sb.append(String.format("  %1$s = sitofp i32 %2$s to double\n", nuevoPtr, ptrDer));
                ptrDer = nuevoPtr;
            }
        }

        return new String[]{ptrIzq, ptrDer};
    }

    public static String ejecutarOperacionVectorialCompleta(ast.OperacionBinaria nodo, String refIzq, String refDer, String tamanoArreglo, String codigoALU) {
        StringBuilder sb = new StringBuilder();
        String tipoEstructuraLLVM = "[" + tamanoArreglo + " x double]";

        // Obtenemos los tipos directamente de los hijos
        String tipoIzq = nodo.getE1().getTipo();
        String tipoDer = nodo.getE2().getTipo();

        // Determinamos si es escalar: si el tipo es un número/bool, NO es un array
        // Ajusta esta lógica según cómo devuelva el tamaño tu getTipo() (ej. si devuelve "int" o "[10 x int]")
        /*boolean izqEsEscalar = !tipoIzq.contains("x");
        boolean derEsEscalar = !tipoDer.contains("x");*/
        boolean izqEsEscalar = !tipoIzq.matches("\\d+");
        boolean derEsEscalar = !tipoDer.matches("\\d+");

        String ptrArrayIzq, ptrArrayDer;

        // 1. Procesamiento Izquierdo
        if (!izqEsEscalar) {
            ptrArrayIzq = getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, refIzq.trim()));
        } else {
            String arrayTempIzq = getNewPointer();
            sb.append(generarBloqueBroadcasting(arrayTempIzq, tipoEstructuraLLVM, tamanoArreglo, refIzq, "Izquierdo"));
            ptrArrayIzq = getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, arrayTempIzq));
        }

        // 2. Procesamiento Derecho
        if (!derEsEscalar) {
            ptrArrayDer = getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, refDer.trim()));
        } else {
            String arrayTempDer = getNewPointer();
            sb.append(generarBloqueBroadcasting(arrayTempDer, tipoEstructuraLLVM, tamanoArreglo, refDer, "Derecho"));
            ptrArrayDer = getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, arrayTempDer));
        }

        String temporalResultado = getNewPointer();
        nodo.setIr_ref(temporalResultado);

        sb.append(String.format("  %1$s = alloca %2$s\n", temporalResultado, tipoEstructuraLLVM));
        String ptrResultadoPlano = temporalResultado;
        sb.append(String.format("  call void @operar_arreglos(ptr %1$s, ptr %2$s, ptr %3$s, i32 %4$s, i32 %5$s)\n",
                ptrArrayIzq, ptrArrayDer, ptrResultadoPlano, tamanoArreglo, codigoALU));

        return sb.toString();
    }

    public static String generarBloqueBroadcasting(String arrayTemp, String tipoEstructura, String tamano, String valRef, String lado) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("; --- Broadcasting %1$s (double) ---\n", lado));
        sb.append(String.format("  %1$s = alloca %2$s\n", arrayTemp, tipoEstructura));

        String idx = getNewPointer();
        sb.append(String.format("  %1$s = alloca i32\n", idx));
        sb.append(String.format("  store i32 0, ptr %1$s\n", idx));

        String labelCond = getNewTag();
        String labelBody = getNewTag();
        String labelEnd  = getNewTag();

        sb.append("  br label %" + labelCond + "\n");
        sb.append(labelCond + ":\n");
        String tIdx = getNewPointer();
        sb.append(String.format("  %1$s = load i32, ptr %2$s\n", tIdx, idx));
        String cmp = getNewPointer();
        sb.append(String.format("  %1$s = icmp slt i32 %2$s, %3$s\n", cmp, tIdx, tamano));
        sb.append(String.format("  br i1 %1$s, label %%%2$s, label %%%3$s\n", cmp, labelBody, labelEnd));

        sb.append(labelBody + ":\n");
        String tIdx64 = getNewPointer();
        sb.append(String.format("  %1$s = sext i32 %2$s to i64\n", tIdx64, tIdx));

        String ptrPos = getNewPointer();
        sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 %4$s\n", ptrPos, tipoEstructura, arrayTemp, tIdx64));
        sb.append(String.format("  store double %1$s, ptr %2$s\n", valRef.trim(), ptrPos));

        String nIdx = getNewPointer();
        sb.append(String.format("  %1$s = add i32 %2$s, 1\n", nIdx, tIdx));
        sb.append(String.format("  store i32 %1$s, ptr %2$s\n", nIdx, idx));
        sb.append("  br label %" + labelCond + "\n");

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
        if (!constantesGlobales.contains(declaracion)) {
            constantesGlobales.add(declaracion);
        }
    }


    public static String obtenerConstantesGlobales() {
        StringBuilder sb = new StringBuilder();
        for (String constante : constantesGlobales) {
            sb.append(constante);
        }
        return sb.toString();
    }
}