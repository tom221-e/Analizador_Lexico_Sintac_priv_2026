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

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Reservar memoria para un entero de 32 bits (alloca i32)
        // Se usa i32 porque scanf necesita 4 bytes completos para escribir el número
        String ptrDest = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = alloca i32\n", ptrDest));

        // 2. Llamar a scanf usando el formato de enteros @int_read_format ("%d")
        String tempCall = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = call i32 (i8*, ...) @scanf(i8* getelementptr inbounds ([4 x i8], [4 x i8]* @int_read_format, i64 0, i64 0), i32* %2$s)\n",
                tempCall, ptrDest));

        // 3. Cargar el entero leído desde la memoria a un registro virtual
        String enteroLeido = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = load i32, i32* %2$s\n",
                enteroLeido, ptrDest));

        // 4. Truncar el i32 a i1 (Boolean real de LLVM: 0 = false, cualquier otro = true)
        String valorBool = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = trunc i32 %2$s to i1\n",
                valorBool, enteroLeido));

        // 5. Guardamos la referencia del registro i1 para que lo use el AST
        this.setIr_ref(valorBool);

        return resultado.toString();
    }

    /*public String getTipo() {
        return "i1"; // Ideal para las operaciones lógicas (AND/OR) o condiciones del IF
    }*/
    
    @Override
    public String getTipo() {
        return "BOOLEAN";  // era "i1"
    }
}