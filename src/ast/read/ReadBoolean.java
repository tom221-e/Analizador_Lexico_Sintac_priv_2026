package ast.read;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class ReadBoolean extends Expresion {

    public ReadBoolean() {
        // No requiere lógica en el constructor
    }

    @Override
    protected String getEtiqueta() {
        // Esta es la etiqueta exacta que pediste
        return "READ: BOOLEAN";
    }

    @Override
    protected String graficar(String idPadre) {
        // Llama al graficador de la superclase (Nodo/Expresion)
        // para crear el nodo con la etiqueta "READ: INT" y conectarlo
        return super.graficar(idPadre);
    }

    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Reservar memoria para el entero (alloca)
        // Guardamos la referencia a este espacio en ir_ref para poder hacer el load luego
        String ptrDest = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("%1$s = alloca i1\n", ptrDest));

        // 2. Llamar a scanf
        // Usamos la global @int_read_format que tu guía pide declarar al inicio
        String tempCall = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("%1$s = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([3 x i8], [3 x i8]* @float_read_format, i64 0, i64 0), i32* %2$s)\n",
                tempCall, ptrDest));

        // 3. Cargar el valor leído en un registro virtual (load)
        // Este es el valor que el nodo de expresión debe exponer hacia afuera
        String valorLeido = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("%1$s = load float, float* %2$s\n",
                valorLeido, ptrDest));

        String valorBool = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = trunc i32 %2$s to i1\n",
                valorBool, valorLeido));

        // 5. Guardamos el resultado final (tipo i1) en ir_ref para uso del AST
        this.setIr_ref(valorBool);

        return resultado.toString();
    }
}