package ast;

import llvm.CodeGeneratorHelper;

public class AccesoArray extends Expresion {
    private final String nombre;
    private final Expresion indice;

    public AccesoArray(String nombre, Expresion indice) {
        this.nombre = nombre;
        this.indice = indice;
    }

    @Override
    protected String getEtiqueta() {
        return "ACCESO ARRAY: " + nombre + "["+indice.getEtiqueta()+"]";
    }
    public String getId() {
        return nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        // CREAMOS UN ID EXCLUSIVO PARA EL GRAFO
        // Este ID muere cuando termina la función, no afecta a nadie más
        String idVisual = "acceso_" + System.identityHashCode(this);

        StringBuilder dot = new StringBuilder();

        // 1. En lugar de usar super.graficar, escribimos la línea del DOT manualmente
        // para usar nuestro idVisual único.
        dot.append(String.format("%s [label=\"%s\"];\n", idVisual, getEtiqueta()));
        dot.append(String.format("%s -> %s;\n", idPadre, idVisual));

        return dot.toString();
    }
    @Override
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código del índice
        resultado.append(this.indice.generarCodigo());

        // 2. Puntero para la dirección calculada
        String ptrDireccion = CodeGeneratorHelper.getNewPointer();

        resultado.append(String.format("%1$s = getelementptr inbounds float, float* %2$s, i32 %3$s\n",
                ptrDireccion, this.nombre, this.indice.getIr_ref()));

        // 4. Registro final para el valor
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 5. Cargar el valor
        resultado.append(String.format("%1$s = load float, float* %2$s\n",
                this.getIr_ref(), ptrDireccion));

        return resultado.toString();
    }
}