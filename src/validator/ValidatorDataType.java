package validator;

import ast.relacional.Menor;
import parser.SymbolTable;
import ast.*;
import ast.literal.*;

/**
 * Clase encargada de la validación semántica de tipos.
 * Asegura que las operaciones y asignaciones sean lógicamente coherentes.
 */
public class ValidatorDataType {

    // --- FUNCIÓN 1: VALIDAR ARITMÉTICA (+, -, *, /) ---
    /**
     * Determina si una operación entre dos expresiones es válida y qué tipo resulta.
     * Ejemplo: INT + FLOAT resulta en FLOAT.
     */
    public String validarAritmetica(Expresion e1, Expresion e2, SymbolTable tabla) {
        // Extraemos la información (tipo y dimensión) de ambos operandos
        InfoNodo info1 = obtenerInfo(e1, tabla);
        InfoNodo info2 = obtenerInfo(e2, tabla);

        // 1. Regla: Restricción de tipos no numéricos
        // No tiene sentido sumar "true + 5"
        if (info1.tipo.equals("BOOLEAN") || info2.tipo.equals("BOOLEAN")) {
            //System.err.println("Error Semántico: No se puede realizar aritmética con BOOLEAN.");
            return "BOOLEAN"; // El null indica que la operación no es válida
        }

        // 2. Regla: Verificación de dimensiones para Arreglos
        // Si ambos son arreglos, deben tener el mismo tamaño para poder operarse entre sí
        if (info1.dimension > 0 && info2.dimension > 0) {
            if (info1.dimension != info2.dimension) {
                System.err.println("Error: Dimensiones incompatibles (" + info1.dimension + " vs " + info2.dimension + ").");
                return null;
            }
        }

        // 3. Regla: Promoción de tipos (Coerción)
        // Si cualquiera de los dos es FLOAT, el resultado se "asciende" a FLOAT para no perder decimales
        if (info1.tipo.equals("FLOAT") || info2.tipo.equals("FLOAT")) {
            return "FLOAT";
        }

        // Si ninguno es FLOAT y llegamos aquí, ambos son INT
        return "INT";
    }

    // --- FUNCIÓN 2: VALIDAR ASIGNACIÓN (Variable = Expresión) ---
    /**
     * Verifica si es legal guardar el valor de una expresión en una variable específica.
     */
    public boolean validarAsignacion(String id, Expresion e, SymbolTable tabla) {
        if (id != null) id = id.replace("%", "").replace("@", "").trim();

        if (!tabla.exists(id)) {
            System.err.println("Error Semántico: Variable '" + id + "' no declarada.");
            return false;
        }

        String tipoDestino = tabla.getTipo(id); // "FLOAT_ARRAY"

        // 🌟 Rescatamos el tamaño de la variable destino desde la tabla
        int dimDestino = 0;
        String tamDestinoStr = tabla.getTamano(id);
        if (tamDestinoStr != null && tamDestinoStr.matches("\\d+")) {
            dimDestino = Integer.parseInt(tamDestinoStr);
        }

        // Información de la expresión de la derecha (la Suma)
        InfoNodo infoExp = obtenerInfo(e, tabla);

        // Si el destino es un arreglo (ej: mediciones)
        if ("FLOAT_ARRAY".equals(tipoDestino) || "INT_ARRAY".equals(tipoDestino)) {
            // Subcaso 1: Asignarle un escalar directo (Broadcasting directo: mediciones = 5.0)
            if (infoExp.dimension == 0) {
                return "INT".equals(infoExp.tipo) || "FLOAT".equals(infoExp.tipo);
            }
            // Subcaso 2: Asignarle el resultado de una operación (mediciones = mediciones + a)
            // Validamos que la dimensión calculada por la suma coincida con el tamaño del arreglo destino
            return dimDestino == infoExp.dimension;
        }

        // Si el destino es un escalar pero le intentas meter un arreglo
        if (infoExp.dimension > 0) {
            System.err.println("Error Semántico: No se puede asignar un arreglo a un escalar.");
            return false;
        }

        if ("FLOAT".equals(tipoDestino) && "INT".equals(infoExp.tipo)) return true;
        return tipoDestino.equals(infoExp.tipo);
    }


    public String obtenerTipoResultante(String id, Expresion e, SymbolTable tabla) {
        if (id != null) id = id.replace("%", "").replace("@", "").trim();

        if (!tabla.exists(id)) {
            return "ERROR_NO_DECLARADO";
        }

        String tipoDestino = tabla.getTipo(id);

        int dimDestino = 0;
        String tamDestinoStr = tabla.getTamano(id);
        if (tamDestinoStr != null && tamDestinoStr.matches("\\d+")) {
            dimDestino = Integer.parseInt(tamDestinoStr);
        }

        InfoNodo infoExp = obtenerInfo(e, tabla);

        if ("FLOAT_ARRAY".equals(tipoDestino) || "INT_ARRAY".equals(tipoDestino)) {
            if (infoExp.dimension == 0) {
                if ("INT".equals(infoExp.tipo) || "FLOAT".equals(infoExp.tipo)) return tipoDestino;
                return "ERROR_TIPO_INCOMPATIBLE";
            }
            if (dimDestino == infoExp.dimension) {
                return tipoDestino; // Devuelve exitosamente "FLOAT_ARRAY"
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
    public boolean validarAsignacionCelda(String id, Expresion valor, SymbolTable tabla) {
        String tipoArreglo = tabla.getTipo(id); // ej: "FLOAT_ARRAY"
        // Convertimos el tipo del arreglo al tipo de sus elementos (FLOAT_ARRAY -> FLOAT)
        String tipoBase = tipoArreglo.replace("_ARRAY", "");

        InfoNodo infoVal = obtenerInfo(valor, tabla);

        // Una celda individual solo acepta valores simples, no otros arreglos
        if (infoVal.dimension > 0) {
            System.err.println("Error: No se puede asignar un arreglo completo a una posición individual.");
            return false;
        }

        // Permitimos mezcla de INT y FLOAT en la celda
        if (tipoBase.equals("FLOAT") && infoVal.tipo.equals("INT")) return true;
        if (tipoBase.equals("INT") && infoVal.tipo.equals("FLOAT")) return true;

        return tipoBase.equals(infoVal.tipo);
    }

    // --- HELPER: INSPECTOR DE NODOS (Recursivo) ---
    /**
     * Analiza cualquier nodo del árbol (Expresion) y deduce su tipo y dimensión.
     * Este es el "corazón" que recorre el árbol.
     */
    public InfoNodo obtenerInfo(Expresion n, SymbolTable tabla) {
        if (n == null) return new InfoNodo("ERROR", 0);

        // 1. Si es un valor fijo (Literal)
        if (n instanceof IntLiteral) return new InfoNodo("INT", 0);
        if (n instanceof FloatLiteral) return new InfoNodo("FLOAT", 0);
        if (n instanceof BoolLiteral) return new InfoNodo("BOOLEAN", 0);

        // 2. Si es una variable (IdLiteral)
        if (n instanceof IdLiteral) {
            String id = ((IdLiteral) n).getNombreVariable();

            // 🌟 CORRECCIÓN 1: Limpiar los prefijos % o @ de LLVM para poder encontrar el ID en la tabla
            if (id != null) {
                id = id.replace("%", "").replace("@", "").trim();
            }

            if (tabla == null) {
                tabla = parser.Parser.tablaSimbolos;
            }
            String t = tabla.getTipo(id);

            if (t == null) {
                System.err.println("Error Semántico: Variable '" + id + "' no declarada.");
                return new InfoNodo("ERROR", 0);
            }

            // 🌟 CORRECCIÓN 2: Extraer y convertir el String de getTamano() a un int numérico
            int d = 0;
            if ("FLOAT_ARRAY".equals(t) || "INT_ARRAY".equals(t)) {
                String tamanoStr = tabla.getTamano(id); // Recibe el String de la tabla (ej: "10")

                if (tamanoStr != null && tamanoStr.matches("\\d+")) {
                    d = Integer.parseInt(tamanoStr); // Convierte el String a int real de Java
                }

                System.out.println("   [VALIDADOR] Variable: " + id + " | Tipo: " + t + " | Dimensión real: " + d);
                // Retornamos el tipo base (FLOAT o INT) junto con su tamaño > 0
                return new InfoNodo("FLOAT_ARRAY".equals(t) ? "FLOAT" : "INT", d);
            }

            // Si es un escalar común (INT, FLOAT, BOOLEAN), su dimensión de arreglo es 0
            return new InfoNodo(t, 0);
        }


        // 3. Si es un acceso a una posición (ej: a[i])
        if (n instanceof AccesoArray) {
            String id = ((AccesoArray) n).getId();
            String t = tabla.getTipo(id);

            if (t == null) {
                System.err.println("Error Semántico: Arreglo '" + id + "' no declarado.");
                return new InfoNodo("ERROR", 0);
            }

            // Al acceder a una celda, la dimensión resultante es 0 (escalar)
            return new InfoNodo(t.replace("_ARRAY", ""), 0);
        }

        // 4. Si es una operación (ej: x + y)
        if (n instanceof OperacionBinaria) {
            Expresion e1 = ((OperacionBinaria)n).getE1();
            Expresion e2 = ((OperacionBinaria)n).getE2();

            // Llamada recursiva para validar la aritmética de los hijos
            String tRes = validarAritmetica(e1, e2, tabla);

            if (tRes == null) return new InfoNodo("ERROR", 0);

            InfoNodo i1 = obtenerInfo(e1, tabla);
            InfoNodo i2 = obtenerInfo(e2, tabla);

            // REPARADO: Si la operación binaria es en realidad una comparación relacional (como Menor, Mayor, etc.)
            // según tu regla debe retornar BOOLEAN y dimensión escalar 0 si ambas dimensiones coinciden.
            if (n instanceof Menor || n.getClass().getSimpleName().contains("Mayor") || n.getClass().getSimpleName().contains("Igual")) {
                if (i1.dimension != i2.dimension) {
                    System.err.println("Error Semántico: Dimensiones incompatibles en la comparación.");
                    return new InfoNodo("ERROR", 0);
                }
                return new InfoNodo("BOOLEAN", 0);
            }

            // El resultado hereda la dimensión mayor (por si hay un escalar operando con un arreglo)
            return new InfoNodo(tRes, Math.max(i1.dimension, i2.dimension));
        }

        // 5. Casos especiales de funciones del lenguaje
        if (n instanceof ValorMasCercano) {
            // Esta función específica siempre retorna un número flotante simple
            return new InfoNodo("FLOAT", 0);
        }

        return new InfoNodo("UNKNOWN", 0);
    }

    /**
     * Estructura de datos interna para mover la info por el árbol.
     */
    public static class InfoNodo {
        String tipo;      // INT, FLOAT, BOOLEAN, ERROR
        int dimension;    // 0 para escala, >0 para arreglos

        InfoNodo(String t, int d) {
            this.tipo = t;
            this.dimension = d;
        }
        public Integer getDim() { return dimension; }
        public String getTipo() { return tipo; }
    }

    public String obtenerTipoLLVM(Expresion e, SymbolTable tabla) {
        InfoNodo info = obtenerInfo(e, tabla);

        if (info == null || "ERROR".equals(info.getTipo())) {
            return "unknown";
        }

        switch (info.getTipo()) {
            case "INT":
                return "i32";
            case "FLOAT":
                return "float";
            case "BOOLEAN":
                return "i1";
            default:
                return "unknown";
        }
    }
    public String obtenerTipo(Expresion e, SymbolTable tabla) {
        InfoNodo info = obtenerInfo(e, tabla);
        return info.getTipo();
    }
}
