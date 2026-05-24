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

        // 1. Generamos el código de los hijos (recursividad)
        resultado.append(this.izquierda.generarCodigo());
        resultado.append(this.derecha.generarCodigo());

        // DETECCIÓN: ¿this.tipo es un número? Si es un número, es una dimensión de arreglo.
        boolean esOperacionDeArreglo = this.tipo.matches("\\d+");

        if (esOperacionDeArreglo) {
            String tamanoArreglo = this.tipo; // "2", "5", "10", etc.
            String tipoEstructuraLLVM = "[" + tamanoArreglo + " x float]";

            // Mapeamos el código de la operación (Suma=1, Resta=2, División=3, Multiplicación=4)
            String opCode = this.get_llvm_op_code();
            String codigoOperacion = "1"; // Por defecto suma

            if (opCode.contains("sub") || opCode.contains("resta") || opCode.equals("2")) codigoOperacion = "2";
            else if (opCode.contains("div") || opCode.equals("3")) codigoOperacion = "3";
            else if (opCode.contains("mul") || opCode.equals("4")) codigoOperacion = "4";

            // Variables que guardarán los punteros finales que se enviarán a la ALU
            String ptrArrayIzq = this.izquierda.getIr_ref();
            String ptrArrayDer = this.derecha.getIr_ref();

            // DETERMINACIÓN DE BROADCASTING SIN VALIDADOR:
            // Si el IR_ref del hijo NO contiene un puntero de arreglo (un getelementptr previo o asignación de array),
            // o si es un registro cargado de float escalar o un número plano.
            // Una variable de arreglo en tu IdLiteral retorna algo como "%miArray". Un escalar cargado genera un nuevo puntero temporal de carga.
            // Para estar seguros: si el nodo hijo no está marcado explícitamente o si es una operación escalar previa, se infla.
            boolean izqEsEscalar = !ptrArrayIzq.contains("data") && !ptrArrayIzq.contains("Array") && !ptrArrayIzq.contains("result");
            boolean derEsEscalar = !ptrArrayDer.contains("data") && !ptrArrayDer.contains("Array") && !ptrArrayDer.contains("result");

            // Si ambos son arrays por descarte, pero tu lenguaje permite operar Array vs Escalar,
            // forzamos broadcasting si el IR_ref del hijo corresponde a un temporal escalar (ej: %t1 de un load float)

            // -----------------------------------------------------------------
            // CASO ESCALAR EN LA IZQUIERDA: Convertir a Array Temporal
            // -----------------------------------------------------------------
            if (izqEsEscalar) {
                String arrayTempIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append("; --- Broadcasting Dinámico: Inflar Escalar Izquierdo ---\n");
                resultado.append(String.format("%1$s = alloca %2$s\n", arrayTempIzq, tipoEstructuraLLVM));

                // Ciclo en LLVM para rellenar el array temporal con el valor escalar
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

            // -----------------------------------------------------------------
            // CASO ESCALAR EN LA DERECHA: Convertir a Array Temporal
            // -----------------------------------------------------------------
            if (derEsEscalar) {
                String arrayTempDer = CodeGeneratorHelper.getNewPointer();
                resultado.append("; --- Broadcasting Dinámico: Inflar Escalar Derecho ---\n");
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

            // -----------------------------------------------------------------
            // EJECUCIÓN FINAL DE LA ALU
            // -----------------------------------------------------------------
            String temporalResultado = CodeGeneratorHelper.getNewPointer();
            this.setIr_ref(temporalResultado);

            resultado.append("; --- Llamada final a la función ALU ---\n");
            resultado.append(String.format("%1$s = alloca %2$s\n", temporalResultado, tipoEstructuraLLVM));

            String ptrResultadoPlano = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("%1$s = getelementptr %2$s, %2$s* %3$s, i32 0, i32 0\n",
                    ptrResultadoPlano, tipoEstructuraLLVM, temporalResultado));

            resultado.append(String.format("call void @operar_arreglos(float* %1$s, float* %2$s, float* %3$s, i32 %4$s, i32 %5$s)\n",
                    ptrArrayIzq, ptrArrayDer, ptrResultadoPlano, tamanoArreglo, codigoOperacion));

        } else {
            // === CASO ESCALAR NORMAL ===
            this.setIr_ref(CodeGeneratorHelper.getNewPointer());

            resultado.append(String.format("%1$s = %2$s %3$s %4$s, %5$s\n",
                    this.getIr_ref(),
                    this.get_llvm_op_code(),
                    this.tipo, // Ej: "float" o "i32"
                    this.izquierda.getIr_ref(),
                    this.derecha.getIr_ref()
            ));
        }

        return resultado.toString();
    }
}