package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa la información de un símbolo en la tabla.
 */
record SymbolInfo(String token, String type, String valor, String longitud) {}

public class SymbolTable {
    private HashMap<String, SymbolInfo> table;

    public SymbolTable() {
        this.table = new HashMap<>();
    }

    public void add(String token, String id, String type, String valor, String longitud) {
        String valorFinal = (valor == null) ? "-" : valor;
        String longitudFinal;

        // Lógica de longitud
        if (valor == null) {
            longitudFinal = (longitud == null) ? "-" : longitud;
        } else {
            longitudFinal = (longitud == null) ? String.valueOf(valor.length()) : longitud;
        }

        // --- MANEJO DE LLAVE (ID) ---
        // Si el ID es nulo (como en un CTE_STR), usamos el valor como llave
        // para que no se sobreescriban y aparezcan en el print.
        String key = (id != null) ? id : "_" + valorFinal;

        table.put(key, new SymbolInfo(token, type, valorFinal, longitudFinal));
    }

    public boolean exists(String id) {
        return table.containsKey(id);
    }

    public void print() {
        System.out.println("--------------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-15s | %-15s | %-15s | %-10s %n",
                "NOMBRE", "TOKEN", "TIPO", "VALOR", "LONGITUD");
        System.out.println("--------------------------------------------------------------------------------------");

        for (Map.Entry<String, SymbolInfo> entry : table.entrySet()) {
            String id = entry.getKey();
            SymbolInfo info = entry.getValue();

            System.out.printf("%-15s | %-15s | %-15s | %-15s | %-10s %n",
                    id,
                    info.token(),
                    info.type(),
                    info.valor(),
                    info.longitud());
        }

        System.out.println("--------------------------------------------------------------------------------------");
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
}