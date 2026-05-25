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
}