package ast;

import llvm.CodeGeneratorHelper;

public class AccesoArray extends Expresion {
    private final String nombre;
    private final Expresion indice;
    private final String tamano;

    public AccesoArray(String nombre, Expresion indice, String tamano) {
        this.nombre = nombre;
        this.indice = indice;
        this.tamano = tamano;
    }

    @Override
    protected String getEtiqueta() {
        return "ACCESO ARRAY: " + nombre + "[" + indice.getEtiqueta() + "]";
    }

    public String getId() {
        return nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        String idVisual = "acceso_" + System.identityHashCode(this);
        StringBuilder dot = new StringBuilder();

        dot.append(String.format("%s [label=\"%s\"];\n", idVisual, getEtiqueta()));
        dot.append(String.format("%s -> %s;\n", idPadre, idVisual));

        return dot.toString();
    }

    protected String getNombreP() {
        return "%" + nombre;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código del índice (recursividad del AST)
        resultado.append(this.indice.generarCodigo());

        // 🌟 CORRECCIÓN 1: Convertir el índice i32 a i64 para evitar desajustes de tipos en punteros de 64-bits
        String ptrIndice64 = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = sext i32 %2$s to i64\n",
                ptrIndice64,
                this.indice.getIr_ref()
        ));

        // 2. Puntero temporal para almacenar la dirección calculada de la celda
        String ptrDireccion = CodeGeneratorHelper.getNewPointer();

        // 🌟 CORRECCIÓN 2: Sintaxis moderna de LLVM usando 'ptr' e índices i64
        String tipoEstructura = "[" + this.tamano + " x double]";
        resultado.append(String.format("  %1$s = getelementptr inbounds %2$s, ptr %3$s, i64 0, i64 %4$s\n",
                ptrDireccion,
                tipoEstructura,
                this.getNombreP(),
                ptrIndice64
        ));

        // 🌟 CORRECCIÓN 3: Eliminado el 'String p' duplicado fantasma.
        // Solicitamos un ÚNICO registro definitivo que contendrá el valor extraído.
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 🌟 CORRECCIÓN 4: Carga opaca nativa con 'ptr' sin asteriscos
        resultado.append(String.format("  %1$s = load double, ptr %2$s\n",
                this.getIr_ref(),
                ptrDireccion
        ));

        return resultado.toString();
    }
}