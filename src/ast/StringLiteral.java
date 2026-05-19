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
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer().replace("%", ""));
        resultado.append(String.format("%1$s =  private constant [%2$s x i8] c%3$s", "@"+ this.getIr_ref(), valor.length(), valor+"\00"));
        return resultado.toString();
    }
}