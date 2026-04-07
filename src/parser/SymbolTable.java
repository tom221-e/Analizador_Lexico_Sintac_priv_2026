package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa la información de un símbolo en la tabla.
 */
record SymbolInfo(String type, String valor, String longitud) {}

public class SymbolTable {
    private HashMap<String, SymbolInfo> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void add(String id, String type, String valor, String longitud) {
        String valorFinal;
        String longitudFinal;

        if (valor == null) {
            valorFinal = null;
            longitudFinal = null;
        } else {
            valorFinal = valor;
            if (longitud == null) {
                longitudFinal = String.valueOf(valor.length());
            } else {
                longitudFinal = longitud;
            }
        }

        table.put(id, new SymbolInfo(type, valorFinal, longitudFinal));
    } // AQUÍ TERMINA EL MÉTODO ADD

    public boolean exists(String id) {
        return table.containsKey(id);
    }

    public void print() {
        System.out.println("-------------------------------------------------------------------");
        System.out.printf("%-15s | %-15s | %-15s | %-10s %n", "ID", "TIPO", "VALOR", "LONGITUD");
        System.out.println("-------------------------------------------------------------------");

        for (Map.Entry<String, SymbolInfo> entry : table.entrySet()) {
            String id = entry.getKey();
            SymbolInfo info = entry.getValue();
            System.out.printf("%-15s | %-15s | %-15s | %-10s %n",
                    id, info.type(), info.valor(), info.longitud());
        }
        System.out.println("-------------------------------------------------------------------");
    }
/**
    public static void main(String[] args) {
        SymbolTable st = new SymbolTable();

        st.add("var1", "int", "10", String.valueOf("10".length()));
        st.add("msg", "string", "\"Hola\"", "20");
        st.add("PI", "double", "3.1416", "8");
        st.add("temp", "float", null, null); // Ejemplo de variable sin valor

        if (st.exists("var1")) {
            System.out.println("¡'var1' existe en la tabla!");
        }

        st.print();
    }
    */
} // AQUÍ TERMINA LA CLASE SYMBOLTABLE