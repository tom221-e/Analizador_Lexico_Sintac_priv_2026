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