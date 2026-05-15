package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class ArrayLiteral extends Expresion {
    private final String valor;

    public ArrayLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Limpiar el string "[1.1, 2.2]" y obtener los elementos individuales
        String limpio = valor.replace("[", "").replace("]", "").trim();
        if (limpio.isEmpty()) return ""; // Arreglo vacío

        String[] elementos = limpio.split("\\s*,\\s*");
        int n = elementos.length;

        // 2. Pedir un puntero para el inicio del arreglo
        // Usamos un tipo de arreglo fijo en LLVM: [N x float]
        String ptrBase = CodeGeneratorHelper.getNewPointer();
        this.setIr_ref(ptrBase);

        // 3. Reservar espacio en el stack: %ptro.1 = alloca [3 x float]
        resultado.append(String.format("%1$s = alloca [%2$s x float]\n", ptrBase, n));

        // 4. Llenar el arreglo posición por posición
        for (int i = 0; i < n; i++) {
            String ptrElemento = CodeGeneratorHelper.getNewPointer();

            // Calculamos la dirección del elemento i:
            // getelementptr [N x float], [N x float]* %base, i32 0, i32 i
            resultado.append(String.format("%1$s = getelementptr [%2$s x float], [%3$s x float]* %4$s, i32 0, i32 %5$s\n",
                    ptrElemento, n, n, ptrBase, i));

            // Guardamos el valor literal (el float) en esa dirección
            resultado.append(String.format("  store float %1$s, float* %2$s\n", elementos[i], ptrElemento));
        }

        return resultado.toString();
    }

    @Override
    protected String getEtiqueta() {
        return "ARRAY: " + valor;
    }
}