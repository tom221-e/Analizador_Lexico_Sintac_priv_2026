package validator;

import ast.read.*;
import ast.relacional.Menor;
import ast.*;
import ast.literal.*;

/**
 * Clase encargada de la validación semántica de tipos de manera auto-contenida.
 * Optimizado: Solo existen arreglos de tipo FLOAT_ARRAY y la dimensión viene inyectada
 * como un String numérico en la variable tipo del IdLiteral.
 */

public class ValidatorDataType {

    // --- FUNCIÓN 1: VALIDAR ARITMÉTICA (+, -, *, /) ---
    /**
     * Determina si una operación entre dos expresiones es válida y qué tipo resulta.
     * Ejemplo: INT + FLOAT resulta en FLOAT.
     */
    public String validarAritmetica(Expresion e1, Expresion e2) {
        InfoNodo info1 = obtenerInfo(e1);
        InfoNodo info2 = obtenerInfo(e2);

        // 1. Regla: Restricción de tipos no numéricos
        if (info1.tipo.equals("BOOLEAN") || info2.tipo.equals("BOOLEAN")) {
            return "BOOLEAN";
        }

        // 2. Regla: Verificación de dimensiones para Arreglos (Ambos FLOAT_ARRAY)
        if (info1.dimension > 0 && info2.dimension > 0) {
            if (info1.dimension != info2.dimension) {
                System.err.println("Error Semántico: Dimensiones de arreglos incompatibles (" + info1.dimension + " vs " + info2.dimension + ").");
                return null;
            }
        }

        // 3. Regla: Promoción de tipos (Coerción)
        if (info1.tipo.equals("FLOAT") || info2.tipo.equals("FLOAT")) {
            return "FLOAT";
        }

        return "INT";
    }

    // --- FUNCIÓN 2: VALIDAR ASIGNACIÓN (Variable = Expresión) ---
    /**
     * Verifica si es legal guardar el valor de una expresión en una variable específica.
     */
    public boolean validarAsignacion(IdLiteral id, Expresion e) {
        if (id == null) return false;

        // Interpretamos el tipo y dimensión a partir del String devuelto por getTipo()
        String tDest = id.getTipo();
        boolean esArray = tDest != null && tDest.matches("\\d+");

        int dimDestino = esArray ? Integer.parseInt(tDest) : 0;
        String tipoDestino = esArray ? "FLOAT_ARRAY" : tDest;

        // Información de la expresión de la derecha
        InfoNodo infoExp = obtenerInfo(e);

        // Si el destino es un Arreglo (FLOAT_ARRAY)
        if (esArray) {
            // Subcaso 1: Broadcasting directo (ej: miArray = 4.5)
            if (infoExp.dimension == 0) {
                return "INT".equals(infoExp.tipo) || "FLOAT".equals(infoExp.tipo);
            }
            // Subcaso 2: Asignarle otra expresión de arreglo (ej: miArray = miArray + 2.0)
            return dimDestino == infoExp.dimension;
        }

        // Si el destino es un escalar común pero intentás meterle un arreglo completo
        if (infoExp.dimension > 0) {
            System.err.println("Error Semántico: No se puede asignar un arreglo completo a un escalar '" + id.getNombreVariable() + "'.");
            return false;
        }

        if ("FLOAT".equals(tipoDestino) && "INT".equals(infoExp.tipo)) return true;
        if ("BOOLEAN".equals(tipoDestino) && "BOOLEAN".equals(infoExp.tipo)) return true;

        return tipoDestino.equals(infoExp.tipo);
    }

    /**
     * Calcula y retorna el tipo resultante textualmente de una asignación.
     */
    public String obtenerTipoResultante(IdLiteral id, Expresion e) {
        if (id == null) return "ERROR_NO_DECLARADO";

        String tDest = id.getTipo();
        boolean esArray = tDest != null && tDest.matches("\\d+");

        int dimDestino = esArray ? Integer.parseInt(tDest) : 0;
        String tipoDestino = esArray ? "FLOAT_ARRAY" : tDest;

        InfoNodo infoExp = obtenerInfo(e);

        if (esArray) {
            if (infoExp.dimension == 0) {
                if ("INT".equals(infoExp.tipo) || "FLOAT".equals(infoExp.tipo)) return tipoDestino;
                return "ERROR_TIPO_INCOMPATIBLE";
            }
            if (dimDestino == infoExp.dimension) {
                return tipoDestino;
            }
            return "ERROR_DIMENSION_INCOMPATIBLE";
        }

        if (infoExp.dimension > 0) {
            return "ERROR_ASIGNAR_ARRAY_A_ESCALAR";
        }

        if ("FLOAT".equals(tipoDestino) && "INT".equals(infoExp.tipo)) return "FLOAT";
        if (tipoDestino.equals(infoExp.tipo)) return tipoDestino;

        return "ERROR_TIPO_INCOMPATIBLE";
    }

    /**
     * Valida la asignación a una posición específica (ej: miArray[0] = valor).
     */
    public boolean validarAsignacionCelda(IdLiteral id, Expresion valor) {
        if (id == null) return false;

        String tDest = id.getTipo();
        boolean esArray = tDest != null && tDest.matches("\\d+");

        if (!esArray) {
            System.err.println("Error Semántico: Intentando indexar la variable '" + id.getNombreVariable() + "' que no es un arreglo.");
            return false;
        }

        // Como el único arreglo existente es FLOAT_ARRAY, el tipo base de cualquier celda es siempre FLOAT
        String tipoBase = "FLOAT";
        InfoNodo infoVal = obtenerInfo(valor);

        // Una celda individual no puede recibir otro arreglo completo
        if (infoVal.dimension > 0) {
            System.err.println("Error Semántico: No se puede asignar un arreglo completo a una celda individual.");
            return false;
        }

        // Permitimos asignarle enteros (INT) a la celda decimal (FLOAT) por coerción
        if (tipoBase.equals("FLOAT") && infoVal.tipo.equals("INT")) return true;

        return tipoBase.equals(infoVal.tipo);
    }

    // --- HELPER: INSPECTOR DE NODOS (Deducción de tipos sin getDimension) ---
    /**
     * Analiza cualquier nodo del árbol y deduce su tipo y dimensión de forma autónoma.
     */
    public InfoNodo obtenerInfo(Expresion n) {
        if (n == null) return new InfoNodo("ERROR", 0);

        // 1. Literales Fijos
        if (n instanceof IntLiteral) return new InfoNodo("INT", 0);
        if (n instanceof FloatLiteral) return new InfoNodo("FLOAT", 0);
        if (n instanceof BoolLiteral) return new InfoNodo("BOOLEAN", 0);

        // 2. Identificadores (Variables o Arreglos completos)
        if (n instanceof IdLiteral idLiteral) {
            String t = idLiteral.getTipo();

            if (t == null) {
                System.err.println("Error Semántico: Variable '" + idLiteral.getNombreVariable() + "' no tiene tipo definido.");
                return new InfoNodo("ERROR", 0);
            }

            // 🌟 DETECCIÓN CLAVE: Si el tipo es numérico (ej: "10"), sabemos que es un FLOAT_ARRAY
            if (t.matches("\\d+")) {
                int dimensionArray = Integer.parseInt(t);
                System.out.println("   [VALIDADOR] Detectado FLOAT_ARRAY | Variable: " + idLiteral.getNombreVariable() + " | Tamaño: " + dimensionArray);
                // El tipo interno de sus datos es FLOAT, pero marcamos su dimensión > 0
                return new InfoNodo("FLOAT", dimensionArray);
            }

            // Si es un escalar común (INT, FLOAT o BOOLEAN)
            return new InfoNodo(t, 0);
        }

        // 3. Acceso a una celda (ej: miArray[i])
        if (n instanceof AccesoArray) {
            // Como el único arreglo que maneja tu compilador es FLOAT_ARRAY,
            // extraer una celda individual de él siempre dará como resultado un FLOAT escalar (dimensión 0).
            return new InfoNodo("FLOAT", 0);
        }

        // 4. Operaciones Binarias (ej: x + y)
        if (n instanceof OperacionBinaria operacionBinaria) {
            Expresion e1 = operacionBinaria.getE1();
            Expresion e2 = operacionBinaria.getE2();

            String tRes = validarAritmetica(e1, e2);
            if (tRes == null) return new InfoNodo("ERROR", 0);

            InfoNodo i1 = obtenerInfo(e1);
            InfoNodo i2 = obtenerInfo(e2);

            // Operaciones relacionales / Comparadores (Menor, Mayor, Igualdad)
            if (n instanceof Menor || n.getClass().getSimpleName().contains("Mayor") || n.getClass().getSimpleName().contains("Igual")) {
                if (i1.dimension != i2.dimension) {
                    System.err.println("Error Semántico: Dimensiones incompatibles en operación lógica relacional.");
                    return new InfoNodo("ERROR", 0);
                }
                return new InfoNodo("BOOLEAN", 0);
            }

            // Mantenemos el soporte de Broadcasting: hereda la dimensión del arreglo si se opera con un escalar
            return new InfoNodo(tRes, Math.max(i1.dimension, i2.dimension));
        }

        // 5. Funciones nativas y lecturas por teclado (Nodos Read)
        if (n instanceof ValorMasCercano) return new InfoNodo("FLOAT", 0);
        if (n instanceof ReadFloat)        return new InfoNodo("FLOAT", 0);
        if (n instanceof ReadInt)          return new InfoNodo("INT", 0);
        if (n instanceof ReadBoolean)      return new InfoNodo("BOOLEAN", 0);

        // 6. Operaciones Unarias (ej: -x, !p)
        if (n instanceof OperacionUnaria operacionUnaria) {
            return obtenerInfo(operacionUnaria.getOperando());
        }

        return new InfoNodo("UNKNOWN", 0);
    }

    /**
     * Estructura interna para el flujo de datos del validador.
     */
    public static class InfoNodo {
        String tipo;      // INT, FLOAT, BOOLEAN, ERROR
        int dimension;    // 0 para escalares, >0 representa el tamaño del FLOAT_ARRAY

        InfoNodo(String t, int d) {
            this.tipo = t;
            this.dimension = d;
        }
        public Integer getDim() { return dimension; }
        public String getTipo() { return tipo; }
    }

    public String obtenerTipoLLVM(Expresion e) {
        InfoNodo info = obtenerInfo(e);
        if (info == null || "ERROR".equals(info.getTipo())) return "unknown";

        switch (info.getTipo()) {
            case "INT":     return "i32";
            case "FLOAT":   return "float";
            case "BOOLEAN": return "i1";
            default:        return "unknown";
        }
    }

    public String obtenerTipo(Expresion e) {
        return obtenerInfo(e).getTipo();
    }
}