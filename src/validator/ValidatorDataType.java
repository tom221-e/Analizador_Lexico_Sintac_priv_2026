package validator;

import parser.SymbolTable;
import ast.*;
import ast.literal.*;

public class ValidatorDataType {

    // --- FUNCIÓN 1: VALIDAR ARITMÉTICA (+, -, *, /) ---
    public String validarAritmetica(Expresion e1, Expresion e2, SymbolTable tabla) {
        InfoNodo info1 = obtenerInfo(e1, tabla);
        InfoNodo info2 = obtenerInfo(e2, tabla);

        // 1. Regla: Prohibido usar booleanos en aritmética
        if (info1.tipo.equals("BOOLEAN") || info2.tipo.equals("BOOLEAN")) {
            System.err.println("Error Semántico: No se puede realizar aritmética con BOOLEAN.");
            return null;
        }

        // 2. Regla: Dimensiones y Broadcasting
        if (info1.dimension > 0 && info2.dimension > 0) {
            if (info1.dimension != info2.dimension) {
                System.err.println("Error: Dimensiones incompatibles (" + info1.dimension + " vs " + info2.dimension + ").");
                return null;
            }
        }

        // 3. Regla: Interoperabilidad (Resultado siempre FLOAT si hay un FLOAT involucrado)
        if (info1.tipo.equals("FLOAT") || info2.tipo.equals("FLOAT")) {
            return "FLOAT";
        }
        return "INT";
    }

    // --- FUNCIÓN 2: VALIDAR ASIGNACIÓN (ID = EXPRESIÓN) ---
    public boolean validarAsignacion(String id, Expresion e, SymbolTable tabla) {
        if (!tabla.exists(id)) {
            System.err.println("Error: Variable '" + id + "' no declarada.");
            return false;
        }

        String tipoDestino = tabla.getTipo(id); // Ejemplo: "INT", "FLOAT", "FLOAT_ARRAY"
        int dimDestino = tabla.getDimension(id);
        InfoNodo infoExp = obtenerInfo(e, tabla);

        // Caso A: El destino es un Arreglo
        if (tipoDestino.equals("FLOAT_ARRAY") || dimDestino > 0) {
            // Regla: Broadcast (Asignar un número simple a todo el arreglo)
            if (infoExp.dimension == 0) {
                return infoExp.tipo.equals("INT") || infoExp.tipo.equals("FLOAT");
            }
            // Regla: Arreglo a Arreglo (Deben tener misma dimensión)
            return dimDestino == infoExp.dimension;
        }

        // Caso B: El destino es un Escalar (INT, FLOAT, BOOLEAN)
        if (infoExp.dimension > 0) {
            System.err.println("Error: No se puede asignar un arreglo a la variable escalar '" + id + "'");
            return false;
        }

        // Regla: Interoperabilidad numérica en escalares
        if (tipoDestino.equals("FLOAT") && infoExp.tipo.equals("INT")) return true;

        return tipoDestino.equals(infoExp.tipo);
    }

    public boolean validarAsignacionCelda(String id, Expresion valor, SymbolTable tabla) {
        String tipoArreglo = tabla.getTipo(id); // Ejemplo: "FLOAT_ARRAY"
        String tipoBase = tipoArreglo.replace("_ARRAY", ""); // Lo volvemos "FLOAT"

        InfoNodo infoVal = obtenerInfo(valor, tabla);

        // No se puede asignar un arreglo a una celda única
        if (infoVal.dimension > 0) {
            System.err.println("Error: No se puede asignar un arreglo completo a una posición individual.");
            return false;
        }

        // Interoperabilidad numérica
        if (tipoBase.equals("FLOAT") && infoVal.tipo.equals("INT")) return true;
        if (tipoBase.equals("INT") && infoVal.tipo.equals("FLOAT")) return true;

        return tipoBase.equals(infoVal.tipo);
    }

    // --- HELPER: INSPECTOR DE NODOS (Sin tocar la clase Expresion) ---
    public InfoNodo obtenerInfo(Expresion n, SymbolTable tabla) {
        if (n == null) return new InfoNodo("ERROR", 0); // Protección contra nodos nulos

        if (n instanceof IntLiteral) return new InfoNodo("INT", 0);
        if (n instanceof FloatLiteral) return new InfoNodo("FLOAT", 0);
        if (n instanceof BoolLiteral) return new InfoNodo("BOOLEAN", 0);

        if (n instanceof IdLiteral) {
            String id = ((IdLiteral) n).getNombreVariable();
            String t = tabla.getTipo(id);

            // CORRECCIÓN 1: Validar que la variable existe en la tabla
            if (t == null) {
                System.err.println("Error Semántico: Variable '" + id + "' no declarada.");
                return new InfoNodo("ERROR", 0);
            }

            int d = tabla.getDimension(id);

            // CORRECCIÓN 2: Comparación segura (evita el NPE si t fuera null)
            if ("FLOAT_ARRAY".equals(t)) return new InfoNodo("FLOAT", d);
            if ("INT_ARRAY".equals(t)) return new InfoNodo("INT", d);

            return new InfoNodo(t, d);
        }

        if (n instanceof AccesoArray) {
            String id = ((AccesoArray) n).getId();
            String t = tabla.getTipo(id);

            if (t == null) {
                System.err.println("Error Semántico: Arreglo '" + id + "' no declarado.");
                return new InfoNodo("ERROR", 0);
            }

            // Quitamos el sufijo _ARRAY para obtener el tipo base (ej: FLOAT_ARRAY -> FLOAT)
            return new InfoNodo(t.replace("_ARRAY", ""), 0);
        }

        if (n instanceof OperacionBinaria) {
            // CORRECCIÓN 3: Manejo de errores en cascada
            Expresion e1 = ((OperacionBinaria)n).getE1();
            Expresion e2 = ((OperacionBinaria)n).getE2();

            String tRes = validarAritmetica(e1, e2, tabla);

            // Si la validación aritmética falló, propagamos el ERROR
            if (tRes == null) return new InfoNodo("ERROR", 0);

            InfoNodo i1 = obtenerInfo(e1, tabla);
            InfoNodo i2 = obtenerInfo(e2, tabla);

            // El resultado tiene el tipo de la operación y la dimensión mayor (Broadcasting)
            return new InfoNodo(tRes, Math.max(i1.dimension, i2.dimension));
        }
        if (n instanceof ValorMasCercano) {
            // Como devuelve un float extraído del array, es FLOAT y es Escalar (dim 0)
            return new InfoNodo("FLOAT", 0);
        }

        return new InfoNodo("UNKNOWN", 0);
    }

    // Clase interna para transportar datos
    public static class InfoNodo {
        String tipo;
        int dimension;
        InfoNodo(String t, int d) { this.tipo = t; this.dimension = d; }

        public String getTipo() {
            return tipo;
        }
    }
}