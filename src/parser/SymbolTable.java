package parser;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa la información de un símbolo en la tabla.
 */
record SymbolInfo(String nombreOriginal, String token, String type, String valor, String longitud) {}

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
            longitudFinal = (longitud == null) ? String.valueOf(valorFinal.length()) : longitud;
        }

        // --- MANEJO DE LLAVE (KEY) INTERNA ---
        // Usamos una clave única interna para el HashMap basada en el valor si es constante.
        // Esto evita duplicados y permite que convivan múltiples constantes con nombre "-".
        String key;
        if (id != null && !id.equals("-")) {
            key = id; // Es una variable (ID real)
        } else {
            key = "_str_" + valorFinal; // Es una constante literal
        }

        // Guardamos el 'id' original ("-" o el nombre de la variable) en el record
        table.put(key, new SymbolInfo(id, token, type, valorFinal, longitudFinal));
    }

    public boolean exists(String id) {
        return table.containsKey(id);
    }

    /**
     * Busca si el contenido del string ya existe en la tabla.
     */
    public boolean existsConstant(String valorBuscado) {
        return table.containsKey("_str_" + valorBuscado);
    }

    public void print() {
        System.out.println("--------------------------------------------------------------------------------------");
        System.out.printf("%-15s | %-15s | %-15s | %-15s | %-10s %n",
                "NOMBRE", "TOKEN", "TIPO", "VALOR", "LONGITUD");
        System.out.println("--------------------------------------------------------------------------------------");

        for (SymbolInfo info : table.values()) {
            // Usamos el nombreOriginal guardado en el record ("-" para constantes)
            String nombreAMostrar = (info.nombreOriginal() == null) ? "-" : info.nombreOriginal();

            System.out.printf("%-15s | %-15s | %-15s | %-15s | %-10s %n",
                    nombreAMostrar,
                    info.token(),
                    info.type(),
                    info.valor(),
                    info.longitud());
        }

        System.out.println("--------------------------------------------------------------------------------------");
    }
}