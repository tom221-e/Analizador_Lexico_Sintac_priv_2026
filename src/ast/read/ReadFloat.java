package ast.read;

import ast.Expresion;
import llvm.CodeGeneratorHelper;

public class ReadFloat extends Expresion {

    public ReadFloat() {
        // No requiere lógica en el constructor
    }

    @Override
    protected String getEtiqueta() {
        // Esta es la etiqueta exacta que pediste
        return "READ: Float";
    }
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Reservar memoria para el entero (alloca)
        // Guardamos la referencia a este espacio en ir_ref para poder hacer el load luego
        String ptrDest = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("%1$s = alloca float\n", ptrDest));

        // 2. Llamar a scanf
        // Usamos la global @int_read_format que tu guía pide declarar al inicio
        String tempCall = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("%1$s = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @float_read_format, i64 0, i64 0), float* %2$s)\n",
                tempCall, ptrDest));

        // 3. Cargar el valor leído en un registro virtual (load)
        // Este es el valor que el nodo de expresión debe exponer hacia afuera
        String valorLeido = CodeGeneratorHelper.getNewPointer();
        String valor = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("%1$s = load float, float* %2$s\n",
                valor, ptrDest));
        resultado.append(String.format("  %1$s = sitofp i32 %2$s to double\n",
                valorLeido, valor));

        // Guardamos este registro en ir_ref para que si haces "x = readInt()",
        // el compilador use %value (valorLeido) como resultado de la expresión
        this.setIr_ref(valorLeido);

        return resultado.toString();
    }
    @Override
    protected String graficar(String idPadre) {
        // Llama al graficador de la superclase (Nodo/Expresion)
        // para crear el nodo con la etiqueta "READ: INT" y conectarlo
        return super.graficar(idPadre);
    }
}