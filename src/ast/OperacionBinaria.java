package ast;

import llvm.*;

public abstract class OperacionBinaria extends Expresion {
    protected final Expresion izquierda;
    protected final Expresion derecha;
    protected final String tipo;


    public OperacionBinaria(Expresion izquierda, Expresion derecha, String tipo) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.tipo = tipo;
    }

    @Override
    protected String getEtiqueta() {
        return String.format("%s", this.getNombreOperacion());
    }

    public Expresion getE1() {
        return izquierda;
    }
    public Expresion getE2() {
        return derecha;
    }

    protected abstract String getNombreOperacion();

    @Override
    protected String graficar(String idPadre) {
        final String miId = this.getId();
        return super.graficar(idPadre) +
                izquierda.graficar(miId) +
                derecha.graficar(miId);
    }
    public abstract String get_llvm_op_code();

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos recursivamente el código de los hijos
        resultado.append(this.izquierda.generarCodigo());
        resultado.append(this.derecha.generarCodigo());

        boolean esOperacionDeArreglo = this.tipo.matches("\\d+");

        if (esOperacionDeArreglo) {
            // =================================================================
            // CASO VECTORIAL / ARREGLOS (Mantiene intacta tu lógica)
            // =================================================================
            String tamanoArreglo = this.tipo;
            String tipoEstructuraLLVM = "[" + tamanoArreglo + " x float]";
            String opCode = this.get_llvm_op_code();
            String codigoOperacion = "1";

            if (opCode.contains("sub") || opCode.contains("resta") || opCode.equals("2")) codigoOperacion = "2";
            else if (opCode.contains("div") || opCode.equals("3")) codigoOperacion = "3";
            else if (opCode.contains("mul") || opCode.equals("4")) codigoOperacion = "4";

            String ptrArrayIzq = this.izquierda.getIr_ref();
            String ptrArrayDer = this.derecha.getIr_ref();

            boolean izqEsEscalar = !ptrArrayIzq.contains("data") && !ptrArrayIzq.contains("Array") && !ptrArrayIzq.contains("result");
            boolean derEsEscalar = !ptrArrayDer.contains("data") && !ptrArrayDer.contains("Array") && !ptrArrayDer.contains("result");

            if (izqEsEscalar) {
                String arrayTempIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append("; --- Broadcasting Izquierdo ---\n");
                resultado.append(String.format("%1$s = alloca %2$s\n", arrayTempIzq, tipoEstructuraLLVM));
                String idx = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = alloca i32\n", idx));
                resultado.append(String.format("store i32 0, i32* %1$s\n", idx));
                String labelCond = CodeGeneratorHelper.getNewTag();
                String labelBody = CodeGeneratorHelper.getNewTag();
                String labelEnd  = CodeGeneratorHelper.getNewTag();
                resultado.append("br label %" + labelCond + "\n");
                resultado.append(labelCond + ":\n");
                String tIdx = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = load i32, i32* %2$s\n", tIdx, idx));
                String cmp = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = icmp slt i32 %2$s, %3$s\n", cmp, tIdx, tamanoArreglo));
                resultado.append(String.format("br i1 %1$s, label %%%2$s, label %%%3$s\n", cmp, labelBody, labelEnd));
                resultado.append(labelBody + ":\n");
                String ptrPos = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 %4$s\n", ptrPos, tipoEstructuraLLVM, arrayTempIzq, tIdx));
                resultado.append(String.format("store float %1$s, float* %2$s\n", this.izquierda.getIr_ref(), ptrPos));
                String nIdx = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = add i32 %2$s, 1\n", nIdx, tIdx));
                resultado.append(String.format("store i32 %1$s, i32* %2$s\n", nIdx, idx));
                resultado.append("br label %" + labelCond + "\n");
                resultado.append(labelEnd + ":\n");
                ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n", ptrArrayIzq, tipoEstructuraLLVM, arrayTempIzq));
            }

            if (derEsEscalar) {
                String arrayTempDer = CodeGeneratorHelper.getNewPointer();
                resultado.append("; --- Broadcasting Derecho ---\n");
                resultado.append(String.format("%1$s = alloca %2$s\n", arrayTempDer, tipoEstructuraLLVM));
                String idx = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = alloca i32\n", idx));
                resultado.append(String.format("store i32 0, i32* %1$s\n", idx));
                String labelCond = CodeGeneratorHelper.getNewTag();
                String labelBody = CodeGeneratorHelper.getNewTag();
                String labelEnd  = CodeGeneratorHelper.getNewTag();
                resultado.append("br label %" + labelCond + "\n");
                resultado.append(labelCond + ":\n");
                String tIdx = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = load i32, i32* %2$s\n", tIdx, idx));
                String cmp = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = icmp slt i32 %2$s, %3$s\n", cmp, tIdx, tamanoArreglo));
                resultado.append(String.format("br i1 %1$s, label %%%2$s, label %%%3$s\n", cmp, labelBody, labelEnd));
                resultado.append(labelBody + ":\n");
                String ptrPos = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 %4$s\n", ptrPos, tipoEstructuraLLVM, arrayTempDer, tIdx));
                resultado.append(String.format("store float %1$s, float* %2$s\n", this.derecha.getIr_ref(), ptrPos));
                String nIdx = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = add i32 %2$s, 1\n", nIdx, tIdx));
                resultado.append(String.format("store i32 %1$s, i32* %2$s\n", nIdx, idx));
                resultado.append("br label %" + labelCond + "\n");
                resultado.append(labelEnd + ":\n");
                ptrArrayDer = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("%1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n", ptrArrayDer, tipoEstructuraLLVM, arrayTempDer));
            }

            String temporalResultado = CodeGeneratorHelper.getNewPointer();
            this.setIr_ref(temporalResultado);
            resultado.append("; --- Llamada final a la función ALU ---\n");
            resultado.append(String.format("%1$s = alloca %2$s\n", temporalResultado, tipoEstructuraLLVM));
            String ptrResultadoPlano = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("%1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n", ptrResultadoPlano, tipoEstructuraLLVM, temporalResultado));
            resultado.append(String.format("call void @operar_arreglos(float* %1$s, float* %2$s, float* %3$s, i32 %4$s, i32 %5$s)\n",
                    ptrArrayIzq, ptrArrayDer, ptrResultadoPlano, tamanoArreglo, codigoOperacion));

        } else {
            // =================================================================
            // === CASO ESCALAR NORMAL (CON TU REGLA DE OMISIÓN DE TIPO) ===
            // =================================================================
            this.setIr_ref(CodeGeneratorHelper.getNewPointer());

            String opCodeLLVM = this.get_llvm_op_code(); // Ej: "fcmp ogt float", "icmp sgt i32", o "add"
            String refIzq = this.izquierda.getIr_ref();
            String refDer = this.derecha.getIr_ref();

            // DETECCIÓN: ¿El opcode devuelto por la clase hija ya contiene 'icmp' o 'fcmp'?
            boolean esComparacion = opCodeLLVM.contains("icmp") || opCodeLLVM.contains("fcmp");

            if (esComparacion) {
                // === TU PROPUESTA EN ACCIÓN ===
                // Omitimos completamente 'this.tipo' de la plantilla.
                // Usamos un formato de solo 4 argumentos: receptor = instruccion_completa operando1, operando2

                // [Opcional pero altamente recomendado para evitar errores de tipo mixto en LLVM]:
                // Si tu validador detectó FLOAT (fcmp) pero el operando izquierdo resultó ser un entero (como %aux o un entero crudo),
                // inyectamos un 'sitofp' rápido para nivelarlo y que LLVM compile perfectamente.
                boolean esFloat = opCodeLLVM.contains("fcmp") || opCodeLLVM.contains("float");
                boolean izqEsFloat = this.izquierda.toString().contains("float") || refIzq.contains("float");
                boolean derEsFloat = this.derecha.toString().contains("float") || refDer.contains("float");

                if (esFloat) {
                    if (!izqEsFloat && !refIzq.contains(".")) {
                        String tempCast = CodeGeneratorHelper.getNewPointer();
                        resultado.append(String.format("%s = sitofp i32 %s to float\n", tempCast, refIzq));
                        refIzq = tempCast;
                    }
                    if (!derEsFloat && !refDer.contains(".")) {
                        String tempCast = CodeGeneratorHelper.getNewPointer();
                        resultado.append(String.format("%s = sitofp i32 %s to float\n", tempCast, refDer));
                        refDer = tempCast;
                    }
                }

                // Impresión final limpia aplicando tu omisión de tipo:
                resultado.append(String.format("%1$s = %2$s %3$s, %4$s\n",
                        this.getIr_ref(),
                        opCodeLLVM,  // Estampa directo tu cadena: "fcmp ogt float" o "icmp sgt i32"
                        refIzq,
                        refDer
                ));

            } else {
                // Operación aritmética estándar (+, -, *, /)
                // Se mantiene la plantilla normal de 5 argumentos que sí usa 'this.tipo'
                resultado.append(String.format("%1$s = %2$s %3$s %4$s, %5$s\n",
                        this.getIr_ref(),
                        opCodeLLVM,   // "add", "sub", etc.
                        this.tipo,    // "i32" o "float"
                        refIzq,
                        refDer
                ));
            }
        }

        return resultado.toString();
    }
}