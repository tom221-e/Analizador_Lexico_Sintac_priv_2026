package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class ArrayLiteral extends Expresion {
    private final String valor;
    private String nombreDestino = null; // 🌟 Guardará el ID preexistente de la macro

    public ArrayLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    // 🌟 NUEVO: Setter para inyectar el arreglo de la macro antes de generar código
    public void setNombreDestino(String nombreDestino) {
        this.nombreDestino = nombreDestino;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Limpiar el string
        String limpio = valor.replace("[", "").replace("]", "").trim();
        if (limpio.isEmpty()) return "";

        String[] elementos = limpio.split("\\s*,\\s*");
        int n = elementos.length;

        // 2. Determinar el puntero base
        // Si nos dieron un destino, usamos ese (con el % de LLVM). Si no, pedimos uno nuevo.
        String ptrBase = (this.nombreDestino != null) ? "%" + this.nombreDestino : CodeGeneratorHelper.getNewPointer();
        this.setIr_ref(ptrBase);

        // 3. Reservar espacio en el stack SÓLO si no viene de la macro
        if (this.nombreDestino == null) {
            resultado.append(String.format("%1$s = alloca [%2$s x double]\n", ptrBase, n));
        }

        // 4. Llenar el arreglo existente posición por posición utilizando ptr
        for (int i = 0; i < n; i++) {
            String ptrElemento = CodeGeneratorHelper.getNewPointer();

            // getelementptr [N x double], ptr %nombreArreglo, i32 0, i32 i
            resultado.append(String.format("%1$s = getelementptr [%2$s x double], ptr %3$s, i32 0, i32 %4$s\n",
                    ptrElemento, n, ptrBase, i));

            // Forzar formato double (.0)
            String valorFormateado = elementos[i];
            if (!valorFormateado.contains(".")) {
                valorFormateado += ".0";
            }

            // Guardar el valor directamente en la memoria unificada
            resultado.append(String.format("store double %1$s, ptr %2$s\n", valorFormateado, ptrElemento));
        }

        return resultado.toString();
    }

    @Override
    protected String getEtiqueta() { return "ARRAY: " + valor; }
    @Override
    public String getTipo() { return "FLOAT_ARRAY"; }
}