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

        // 2. Determinar el puntero base.
        // Si se llamó setNombreDestino(), escribimos directamente en esa variable ya declarada.
        // Si no, allocamos un temporal (caso de uso standalone).
        String ptrBase = (this.nombreDestino != null)
                ? "%" + this.nombreDestino
                : CodeGeneratorHelper.getNewPointer();
        this.setIr_ref(ptrBase);

        // 3. Reservar espacio en el stack SOLO si no hay destino externo
        if (this.nombreDestino == null) {
            resultado.append(String.format("  %1$s = alloca [%2$d x double]\n", ptrBase, n));
        }

        // 4. Llenar el arreglo posición por posición usando i64 (consistente con el resto del backend)
        for (int i = 0; i < n; i++) {
            String ptrElemento = CodeGeneratorHelper.getNewPointer();

            resultado.append(String.format("  %1$s = getelementptr [%2$d x double], ptr %3$s, i64 0, i64 %4$d\n",
                    ptrElemento, n, ptrBase, i));

            String valorFormateado = elementos[i].trim();
            if (!valorFormateado.contains(".")) {
                valorFormateado += ".0";
            }

            resultado.append(String.format("  store double %1$s, ptr %2$s\n", valorFormateado, ptrElemento));
        }

        return resultado.toString();
    }

    @Override
    protected String getEtiqueta() { return "ARRAY: " + valor; }
    @Override
    public String getTipo() { return "FLOAT_ARRAY"; }
}