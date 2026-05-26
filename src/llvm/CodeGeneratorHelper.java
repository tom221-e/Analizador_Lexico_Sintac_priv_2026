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

    public static String[] castearOperandosAlVuelo(ast.Expresion izquierda, ast.Expresion derecha, String tipoDestinoLLVM, StringBuilder sb) {
        String ptrIzq = izquierda.getIr_ref().trim();
        String ptrDer = derecha.getIr_ref().trim();

        // Solo analizamos si la operación final de LLVM requiere alta precisión (double)
        if ("double".equals(tipoDestinoLLVM)) {
            // Instanciamos tu validador semántico directamente
            validator.ValidatorDataType validador = new validator.ValidatorDataType();

            // Usamos la tabla global de tu Parser, igual que hace tu validador por defecto
            parser.SymbolTable tabla = parser.Parser.tablaSimbolos;

            // Obtenemos la info exacta de cada hijo usando TU lógica recursiva
            validator.ValidatorDataType.InfoNodo infoIzq = validador.obtenerInfo(izquierda, tabla);
            validator.ValidatorDataType.InfoNodo infoDer = validador.obtenerInfo(derecha, tabla);

            // Si el izquierdo es INT, se castea a double
            if (infoIzq != null && "INT".equals(infoIzq.getTipo())) {
                String nuevoPtr = CodeGeneratorHelper.getNewPointer();
                sb.append(String.format("  %1$s = sitofp i32 %2$s to double\n", nuevoPtr, ptrIzq));
                ptrIzq = nuevoPtr;
            }

            // Si el derecho es INT, se castea a double
            if (infoDer != null && "INT".equals(infoDer.getTipo())) {
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

        String ptrArrayIzq = refIzq;
        String ptrArrayDer = refDer;

        // 🌟 SOLUCIÓN: Usamos tu validador oficial para saber si son arreglos o no basándonos en los datos reales
        validator.ValidatorDataType validador = new validator.ValidatorDataType();
        parser.SymbolTable tabla = parser.Parser.tablaSimbolos;

        validator.ValidatorDataType.InfoNodo infoIzq = validador.obtenerInfo(nodo.getE1(), tabla);
        validator.ValidatorDataType.InfoNodo infoDer = validador.obtenerInfo(nodo.getE2(), tabla);

        // Si la dimensión es > 0, significa que es un arreglo. Por ende, NO es escalar.
        boolean izqEsEscalar = (infoIzq.getDim() == 0);
        boolean derEsEscalar = (infoDer.getDim() == 0);

        // 1. Procesamiento Izquierdo
        if (!izqEsEscalar) {
            // Si es un Arreglo Real (como %mediciones), extraemos su puntero inicial con getelementptr
            ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
            sb.append("; --- Obtener puntero plano del arreglo izquierdo ---\n");
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, refIzq.trim()));
        } else {
            // Si es un escalar (un número o variable simple), se genera su bloque de Broadcasting
            String arrayTempIzq = CodeGeneratorHelper.getNewPointer();
            sb.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempIzq, tipoEstructuraLLVM, tamanoArreglo, refIzq, "Izquierdo"));
            ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, arrayTempIzq));
        }

        // 2. Procesamiento Derecho
        if (!derEsEscalar) {
            // Si es un Arreglo Real, extraemos su puntero inicial
            ptrArrayDer = CodeGeneratorHelper.getNewPointer();
            sb.append("; --- Obtener puntero plano del arreglo derecho ---\n");
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, refDer.trim()));
        } else {
            // Si es un escalar, se genera su bloque de Broadcasting
            String arrayTempDer = CodeGeneratorHelper.getNewPointer();
            sb.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempDer, tipoEstructuraLLVM, tamanoArreglo, refDer, "Derecho"));
            ptrArrayDer = CodeGeneratorHelper.getNewPointer();
            sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, arrayTempDer));
        }

        // 3. Generación del contenedor del resultado final
        String temporalResultado = CodeGeneratorHelper.getNewPointer();
        nodo.setIr_ref(temporalResultado);

        sb.append("; --- Llamada final a la función ALU de Arreglos ---\n");
        sb.append(String.format("  %1$s = alloca %2$s\n", temporalResultado, tipoEstructuraLLVM));

        String ptrResultadoPlano = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrResultadoPlano, tipoEstructuraLLVM, temporalResultado));

        // 4. Invocación matemática real a la función externa
        sb.append(String.format("  call void @operar_arreglos(ptr %1$s, ptr %2$s, ptr %3$s, i32 %4$s, i32 %5$s)\n",
                ptrArrayIzq, ptrArrayDer, ptrResultadoPlano, tamanoArreglo, codigoALU));

        return sb.toString();
    }

    /**
     * Genera el bloque LLVM intermedio para realizar el Broadcasting de un escalar a un arreglo.
     * Como el escalar ya viene como un double, se elimina la instrucción de truncado fptrunc.
     */
    public static String generarBloqueBroadcasting(String arrayTemp, String tipoEstructura, String tamano, String valRef, String lado) {
        StringBuilder sb = new StringBuilder();

        // 🌟 CORRECCIÓN: El comentario ahora refleja la persistencia de alta precisión
        sb.append(String.format("; --- Broadcasting %1$s (Mantenido nativamente como double) ---\n", lado));
        sb.append(String.format("  %1$s = alloca %2$s\n", arrayTemp, tipoEstructura));

        // Asignamos y limpiamos el índice del iterador (i32)
        String idx = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = alloca i32\n", idx));
        sb.append(String.format("  store i32 0, ptr %1$s\n", idx));

        // Generamos las etiquetas de salto para el bucle
        String labelCond = CodeGeneratorHelper.getNewTag();
        String labelBody = CodeGeneratorHelper.getNewTag();
        String labelEnd  = CodeGeneratorHelper.getNewTag();

        // --- Bloque Condición ---
        sb.append("  br label %" + labelCond + "\n");
        sb.append(labelCond + ":\n");
        String tIdx = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = load i32, ptr %2$s\n", tIdx, idx));
        String cmp = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = icmp slt i32 %2$s, %3$s\n", cmp, tIdx, tamano));
        sb.append(String.format("  br i1 %1$s, label %%%2$s, label %%%3$s\n", cmp, labelBody, labelEnd));

        // --- Bloque Cuerpo ---
        sb.append(labelBody + ":\n");

        // 🌟 SOLUCCIÓN AL GRÁFICO SSA: Expandimos el iterador tIdx (i32) a i64 para el cálculo de puntero
        String tIdx64 = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = sext i32 %2$s to i64\n", tIdx64, tIdx));

        // Obtenemos la posición de memoria usando el índice i64 expandido
        String ptrPos = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 %4$s\n", ptrPos, tipoEstructura, arrayTemp, tIdx64));

        // 🌟 MIGRADO: Se elimina el 'fptrunc' obsoleto. Se almacena directamente como 'double'
        sb.append(String.format("  store double %1$s, ptr %2$s\n", valRef.trim(), ptrPos));

        // Incrementamos el iterador indexado (i32 habitual)
        String nIdx = CodeGeneratorHelper.getNewPointer();
        sb.append(String.format("  %1$s = add i32 %2$s, 1\n", nIdx, tIdx));
        sb.append(String.format("  store i32 %1$s, ptr %2$s\n", nIdx, idx));
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