package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class Desigual extends OperacionBinaria {

    public Desigual(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "!="; // Corresponde a DESIGUAL
    }

    @Override
    public String get_llvm_op_code() {
        if (this.tipo == null) {
            return "; ERROR: Tipo de dato nulo en la operación\n";
        }

        // Si el tipo es numérico (dimensión), le avisa al Padre que use la ALU de vectores
        if (this.tipo.matches("\\d+")) {
            return "5"; // Cambia este identificador por el número que use tu ALU para "!="
        }

        // Evaluamos el tipo de los operandos para el caso escalar
        switch (this.tipo) {
            case "float":
            case "double":
                return "fcmp one"; // 'one' significa Ordered and Not Equal en LLVM
            case "i32":
            case "boolean":
            default:
                return "icmp ne";  // 'ne' significa Not Equal en LLVM
        }
    }

    /**
     * Genera la instrucción de comparación relacional escalar.
     * Recuerda: Aunque compare enteros o doubles, el registro de salida en LLVM siempre es 'i1' (booleano).
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder resultado = new StringBuilder();

        // 1. Solicitamos un nuevo registro temporal SSA para guardar el resultado booleano (i1)
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 2. Mapeamos el tipo con el que se van a comparar los operandos de entrada (con inicialización y breaks)
        String tipoComparacion = "i32";
        switch (this.tipo) {
            case "float":
                tipoComparacion = "double"; // Comparamos en alta precisión de 64-bits
                break;
            case "i32":
                tipoComparacion = "i32";
                break;
            case "i1":
                tipoComparacion = "i1";     // Las condicionales previas ya son de 1 bit en LLVM
                break;
            default:
                tipoComparacion = "i32";    // Respaldo seguro por defecto
                break;
        }

        String[] operandosSeguros = CodeGeneratorHelper.castearOperandosAlVuelo(
                this.izquierda,
                this.derecha,
                tipoComparacion,
                resultado
        );
        String ptrIzqFinal = operandosSeguros[0]; // Puede ser el original o el nuevo registro 'sitofp'
        String ptrDerFinal = operandosSeguros[1]; // Puede ser el original o el nuevo registro 'sitofp'

        // 2. Solicitamos un nuevo registro temporal SSA para guardar el resultado booleano (i1)
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());


        // Formato limpio: %ptro.resultado = icmp/fcmp condición tipo %ptrIzqFinal, %ptrDerFinal
        resultado.append(String.format("  %1$s = %2$s %3$s %4$s, %5$s\n",
                this.getIr_ref(),
                this.get_llvm_op_code(),
                tipoComparacion,
                ptrIzqFinal,  // <-- Modificado
                ptrDerFinal   // <-- Modificado
        ));

        return resultado.toString();
    }
}