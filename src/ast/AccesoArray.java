package ast;

import llvm.CodeGeneratorHelper;

public class AccesoArray extends Expresion {
    private final String nombre;
    private final Expresion indice;
    private final String tamano; // ¡AGREGADO! Necesario para el getelementptr de LLVM

    // Modificamos el constructor para recibir el tamaño total del arreglo
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

        // 1. Generamos el código del índice (recursividad)
        resultado.append(this.indice.generarCodigo());

        // 2. Puntero temporal para almacenar la dirección calculada de la celda
        String ptrDireccion = CodeGeneratorHelper.getNewPointer();

        // 3. CORRECCIÓN: getelementptr estructurado exactamente igual al de la asignación
        // Necesita saber que es un bloque '[tamano x float]' y usar los dos índices (i32 0, i32 %indice)
        resultado.append(String.format("  %1$s = getelementptr inbounds [%2$s x float], [%2$s x float]* %3$s, i32 0, i32 %4$s\n",
                ptrDireccion,
                this.tamano,
                this.getNombreP(),
                this.indice.getIr_ref()
        ));

        // 4. Registro final que contendrá el valor flotante extraído
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 5. Cargar el valor real desde la dirección calculada al registro de esta Expresión
        resultado.append(String.format("  %1$s = load float, float* %2$s\n",
                this.getIr_ref(),
                ptrDireccion
        ));

        return resultado.toString();
    }
}