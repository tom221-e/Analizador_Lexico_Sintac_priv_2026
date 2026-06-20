package ast.relacional;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class Menor extends OperacionBinaria {

    public Menor(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }
    
    /*public String getTipo(){
        return tipo;
    }*/
    
    @Override
    public String getTipo() {
        return "BOOLEAN";  // el resultado de una comparación siempre es booleano
    }
    
    @Override
    protected String getNombreOperacion() {
        return "<"; // Corresponde a MENOR
    }

    @Override
    public String get_llvm_op_code() {
        if (this.tipo == null) {
            return "; ERROR: Tipo de dato nulo en la operación\n";
        }

        // Si el tipo es numérico (dimensión), le avisa al Padre que use la ALU de vectores
        if (this.tipo.matches("\\d+")) {
            return "9"; // Cambia este identificador por el número que use tu ALU para "<"
        }

        // Evaluamos el tipo de los operandos para determinar el opcode escalar de LLVM
        switch (this.tipo) {
            case "FLOAT":
                return "fcmp olt"; // 'olt' significa Ordered Less Than
            case "INT":
            case "BLOOLEAN":
            default:
                return "icmp slt"; // 'slt' significa Signed Less Than
        }
    }

    /**
     * Genera la instrucción de comparación relacional escalar para "Menor que".
     * El registro resultante devuelto en ir_ref siempre será de tipo 'i1' (booleano).
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder resultado = new StringBuilder();

        // 1. Solicitamos un nuevo registro temporal SSA para guardar el resultado de la comparación (i1)
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 2. Mapeamos el tipo con el que se van a comparar los operandos de entrada
        String tipoComparacion = "i32";
        switch (this.tipo) {
            case "FLOAT":
                tipoComparacion = "double"; // Comparamos en alta precisión de 64-bits
                break;
            case "INT":
                tipoComparacion = "i32";
                break;
            case "BLOOLEAN":
                tipoComparacion = "i1";     // Comparación lógica entre booleanos
                break;
            default:
                tipoComparacion = "i32";    // Respaldo por defecto
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

        // 🌟 CAMBIO 2: Reemplazar 'this.izquierda.getIr_ref()' por las variables finales balanceadas
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