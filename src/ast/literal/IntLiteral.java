package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class IntLiteral extends Expresion {
    private final String valor;

    public IntLiteral(String valor) {
        this.valor = String.valueOf(valor);
    }

    @Override
    public String getTipo() {
        return "i32";
    }

    @Override
    protected String getEtiqueta() {
        return "INT: " + valor;
    }

    // Mantenemos este para tu lógica semántica (operaciones, tablas, etc.)
    public String getValor() {
        return valor;
    }

    @Override
    public Expresion copiar() {
        // Al hacer 'new' aquí, garantizas que es una dirección de memoria nueva
        return new IntLiteral(this.valor);
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        resultado.append(String.format("  %1$s = add i32 0, %2$s\n", this.getIr_ref(), Integer.parseInt(getValor())));
        return resultado.toString();
    }

    @Override
    public String graficar(String idPadre) {
        // 1. Creamos un ID visual que sea ÚNICO para cada objeto 'new IntLiteral'
        // Esto evita que Graphviz junte dos nodos que tengan el mismo valor.
        String idVisual = "int_" + System.identityHashCode(this);

        StringBuilder grafico = new StringBuilder();

        // 2. Definimos el nodo con el ID visual único
        grafico.append(String.format("%1$s[label=\"%2$s\"]\n", idVisual, this.getEtiqueta()));

        // 3. Creamos la conexión desde el padre hacia este ID visual único
        if (idPadre != null) {
            grafico.append(String.format("%1$s->%2$s\n", idPadre, idVisual));
        }

        return grafico.toString();
    }
}