package ast;

import llvm.CodeGeneratorHelper;

public class StringLiteral extends Expresion {
    private final String valor;

    public StringLiteral(String valor) {
        // Limpiamos las comillas para que el grafo se vea mejor
        this.valor = valor.replace("\"", "");
    }

    @Override
    protected String getEtiqueta() {
        return "STRING: " + valor; // Lo que dirá el globo en Graphviz
    }

    @Override
    protected String graficar(String idPadre) {
        // Llama a la lógica de Nodo para crear el círculo y la flecha
        return super.graficar(idPadre);
    }
    public String getStr() {
        return valor;
    }
    public Integer getLongitudStr() {
        return valor.length() + 1;
    }

    @Override
    public String generarCodigo() {
        // 1. Reemplazamos el '%' por '@' para que la referencia se guarde como una global válida de LLVM (Ej: @ptro.134)
        String refGlobal = CodeGeneratorHelper.getNewPointer().replace("%", "@");
        this.setIr_ref(refGlobal);

        // 2. Retornamos directamente la cadena formateada con comillas escapadas y el tipo de longitud correcto (%2$d)
        return String.format("%1$s = private unnamed_addr constant [%2$d x i8] c\"%3$s\\00\"\n",
                this.getIr_ref(),          // %1$s -> @ptro.134
                this.getLongitudStr(),     // %2$d -> Longitud del string (usa %d si es un entero)
                this.valor                 // %3$s -> El texto de la cadena
        );
    }
}