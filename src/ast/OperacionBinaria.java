package ast;

import llvm.*;
import ast.literal.IdLiteral; // <--- Importamos tu clase de variables

public abstract class OperacionBinaria extends Expresion {
    protected final Expresion izquierda;
    protected final Expresion derecha;
    protected final String tipo;

    public OperacionBinaria(Expresion izquierda, Expresion derecha, String tipo) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.tipo = tipo;
    }

    public String getTipo() {
        return tipo;
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


    protected abstract String obtenerCodigoEscalar();

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. RECURSIVIDAD CENTRALIZADA: Evaluamos las expresiones de los hijos primero
        if (this.izquierda != null) {
            resultado.append(this.izquierda.generarCodigo());
        }
        if (this.derecha != null) {
            resultado.append(this.derecha.generarCodigo());
        }

        // 2. DETECCIÓN DE CONTEXTO VECTORIAL (Si el tipo es puramente numérico/dimensión)
        boolean esOperacionDeArreglo = this.tipo != null && this.tipo.matches("\\d+");

        if (esOperacionDeArreglo) {
            // Pasamos los registros de los hijos, la dimensión y el código de operación de la ALU
            resultado.append(CodeGeneratorHelper.ejecutarOperacionVectorialCompleta(
                    this,
                    this.izquierda.getIr_ref(),
                    this.derecha.getIr_ref(),
                    this.tipo,
                    this.get_llvm_op_code()
            ));
        } else {
            // 3. CAMINO ESCALAR: El padre invoca la estructura específica formateada por la clase hija
            resultado.append(this.obtenerCodigoEscalar());
        }

        return resultado.toString();
    }
    // 🌟 NUEVO MÉTODO: Permite que el asignador le diga dónde guardar el resultado vectorial
    public String generarCodigoConDestino(String ptrDestinoReal) {
        StringBuilder resultado = new StringBuilder();

        // 1. Evaluamos los hijos primero
        if (this.izquierda != null) {
            resultado.append(this.izquierda.generarCodigo());
        }
        if (this.derecha != null) {
            resultado.append(this.derecha.generarCodigo());
        }

        boolean esOperacionDeArreglo = this.tipo != null && this.tipo.matches("\\d+");

        if (esOperacionDeArreglo) {
            // 🌟 MODIFICACIÓN CLAVE: En vez de dejar que el helper cree un temporal aleatorio,
            // modificamos temporalmente el comportamiento o llamamos a una versión que use ptrDestinoReal
            String tipoEstructuraLLVM = "[" + this.tipo + " x double]";

            // Reutilizamos tu lógica limpia de broadcasting pero apuntando al destino final
            String ptrArrayIzq = this.izquierda.getIr_ref();
            String ptrArrayDer = this.derecha.getIr_ref();

            validator.ValidatorDataType validador = new validator.ValidatorDataType();
            parser.SymbolTable tabla = parser.Parser.tablaSimbolos;
            validator.ValidatorDataType.InfoNodo infoIzq = validador.obtenerInfo(this.izquierda, tabla);
            validator.ValidatorDataType.InfoNodo infoDer = validador.obtenerInfo(this.derecha, tabla);

            if (infoIzq.getDim() == 0) {
                String arrayTempIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempIzq, tipoEstructuraLLVM, this.tipo, this.izquierda.getIr_ref(), "Izquierdo"));
                ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, arrayTempIzq));
            } else {
                ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, this.izquierda.getIr_ref().trim()));
            }

            if (infoDer.getDim() == 0) {
                String arrayTempDer = CodeGeneratorHelper.getNewPointer();
                resultado.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempDer, tipoEstructuraLLVM, this.tipo, this.derecha.getIr_ref(), "Derecho"));
                ptrArrayDer = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, arrayTempDer));
            } else {
                ptrArrayDer = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, this.derecha.getIr_ref().trim()));
            }

            resultado.append("; --- Invocación Directa a la ALU de Arreglos ---\n");
            resultado.append(String.format("  call void @operar_arreglos(ptr %1$s, ptr %2$s, ptr %3$s, i32 %4$s, i32 %5$s)\n",
                    ptrArrayIzq, ptrArrayDer, ptrDestinoReal, this.tipo, this.get_llvm_op_code()));

            this.setIr_ref(ptrDestinoReal);
        } else {
            resultado.append(this.obtenerCodigoEscalar());
        }

        return resultado.toString();
    }
}