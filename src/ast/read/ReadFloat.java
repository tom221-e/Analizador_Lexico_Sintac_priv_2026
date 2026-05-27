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

        // 1. Reservar memoria local para el double según la especificación de la cátedra
        String ptrDest = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = alloca double\n", ptrDest));

        // 2. Llamar a scanf usando el formato exacto @double_read_format (%lf)
        String tempCall = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @double_read_format, i64 0, i64 0), double* %2$s)\n",
                tempCall, ptrDest));

        // 3. Cargar el valor double leído a un registro virtual para retornarlo
        String valorLeido = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = load double, double* %2$s\n",
                valorLeido, ptrDest));

        // Exponemos el registro resultante hacia el AST
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