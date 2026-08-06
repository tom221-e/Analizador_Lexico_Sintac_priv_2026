package ast;

import ast.literal.IntLiteral;

public class FloatIntAarray extends Expresion {
    private final Expresion escalar;
    private final String tamano;

    public FloatIntAarray(Expresion escalar, String tamano) {
        this.escalar = escalar;
        this.tamano = tamano;
        // Eliminamos "valorNormalizado" de aquí. Si es una variable,
        // su valor solo se conocerá en tiempo de ejecución, no al construir el AST.
    }

    @Override
    public String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        dot.append(String.format("  %1$s [label=\"%2$s\"];\n", miId, getEtiqueta()));
        if (idPadre != null) {
            dot.append(String.format("  %1$s -> %2$s;\n", idPadre, miId));
        }

        if (this.escalar != null) {
            dot.append(this.escalar.graficar(miId));
        }

        return dot.toString();
    }

    @Override
    public String generarCodigo() {
        StringBuilder codigo = new StringBuilder();

        // 1. Generamos el código del escalar (Si es una variable, esto emitirá el 'load')
        codigo.append(escalar.generarCodigo());

        // 2. Capturamos la referencia IR del escalar (Ej: %ptro.50, o "5.0")
        String refValor;
        if (escalar instanceof IntLiteral) {
            refValor = ((IntLiteral) escalar).getFloat(); // Casteo seguro a float si es entero
        } else {
            refValor = escalar.getIr_ref();
        }

        int n = Integer.parseInt(tamano);

        // 3. Pedimos un puntero base para el arreglo
        // ⚠️ ATENCIÓN: Reemplaza "getNuevoPuntero()" por el método exacto
        // que uses en tu clase llvm.CodeGeneratorHelper para obtener un temporal (ej. %ptro.51)
        String ptrBase = llvm.CodeGeneratorHelper.getNewPointer();

        codigo.append("  ").append(ptrBase).append(" = alloca [").append(n).append(" x double]\n");

        // 4. Llenamos el arreglo con el valor evaluado
        for (int i = 0; i < n; i++) {
            // ⚠️ ATENCIÓN: Igual que arriba, pide un nuevo puntero temporal
            String ptrElemento = llvm.CodeGeneratorHelper.getNewPointer();

            codigo.append("  ").append(ptrElemento)
                    .append(" = getelementptr [").append(n).append(" x double], ptr ")
                    .append(ptrBase).append(", i64 0, i64 ").append(i).append("\n");

            codigo.append("  store double ").append(refValor)
                    .append(", ptr ").append(ptrElemento).append("\n");
        }

        // 5. Guardamos la referencia base para quien vaya a usar este arreglo
        this.setIr_ref(ptrBase);

        return codigo.toString();
    }

    @Override
    public String getTipo() {
        return "[" + tamano + " x double]";
    }

    @Override
    protected String getEtiqueta() {
        // Mostramos una etiqueta más limpia para el AST
        return "ArrayInit[" + tamano + "]";
    }
}