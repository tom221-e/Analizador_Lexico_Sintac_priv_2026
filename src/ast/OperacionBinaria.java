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

    public String getTipo() {
        return tipo;
    }

    private String tipoLenguaje() {
        if (this.tipo == null) return "?";
        return switch (this.tipo) {
            case "i32"             -> "INT";
            case "float", "double" -> "FLOAT";
            case "i1"              -> "BOOL";
            default -> {
                // si ya es INT/FLOAT/BOOL/dimensión numérica, mostrar directo
                if (this.tipo.matches("\\d+")) yield "ARRAY[" + this.tipo + "]";
                yield this.tipo;
            }
        };
    }

    // Reemplazar getEtiqueta():
    @Override
    protected String getEtiqueta() {
        return String.format("%s (%s)", this.getNombreOperacion(), tipoLenguaje());
    }
    
    /*@Override
    protected String getEtiqueta() {
        return String.format("%s", this.getNombreOperacion());
    }*/

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
    // NUEVO MÉTODO: Permite que el asignador le diga dónde guardar el resultado vectorial
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
            String tipoEstructuraLLVM = "[" + this.tipo + " x double]";

            String ptrArrayIzq;
            String ptrArrayDer;

            String idNameIzq = this.izquierda.getId();
            String idNameDer = this.derecha.getId();

            String tipoIzq = this.izquierda.getTipo();
            String tipoDer = this.derecha.getTipo();

            boolean izqEsEscalar = "INT".equals(tipoIzq) || "i32".equals(tipoIzq) ||
                    "FLOAT".equals(tipoIzq) || "float".equals(tipoIzq) ||
                    "BOOLEAN".equals(tipoIzq) || "i1".equals(tipoIzq);

            boolean derEsEscalar = "INT".equals(tipoDer) || "i32".equals(tipoDer) ||
                    "FLOAT".equals(tipoDer) || "float".equals(tipoDer) ||
                    "BOOLEAN".equals(tipoDer) || "i1".equals(tipoDer);

            // 2. Procesamiento Izquierdo
            if (izqEsEscalar) {
                String arrayTempIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempIzq, tipoEstructuraLLVM, this.tipo, this.izquierda.getIr_ref(), "Izquierdo"));
                ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, arrayTempIzq));
            } else {
                ptrArrayIzq = CodeGeneratorHelper.getNewPointer();
                // 🌟 VALIDACIÓN: Si idName contiene "nodo_", significa que es un ID interno del AST, no la variable real.
                String refRealIzq = (idNameIzq != null && !idNameIzq.isEmpty() && !idNameIzq.contains("nodo_"))
                        ? "%" + idNameIzq
                        : this.izquierda.getIr_ref().trim();

                // Si getIr_ref() ya trae el '%', se lo quitamos para no duplicarlo en el format
                if (refRealIzq.startsWith("%%")) refRealIzq = refRealIzq.substring(1);

                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayIzq, tipoEstructuraLLVM, refRealIzq));
            }

            // 3. Procesamiento Derecho
            if (derEsEscalar) {
                String arrayTempDer = CodeGeneratorHelper.getNewPointer();
                resultado.append(CodeGeneratorHelper.generarBloqueBroadcasting(arrayTempDer, tipoEstructuraLLVM, this.tipo, this.derecha.getIr_ref(), "Derecho"));
                ptrArrayDer = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, arrayTempDer));
            } else {
                ptrArrayDer = CodeGeneratorHelper.getNewPointer();
                // 🌟 VALIDACIÓN: Evitamos usar identificadores internos de nodos como variables de LLVM
                String refRealDer = (idNameDer != null && !idNameDer.isEmpty() && !idNameDer.contains("nodo_"))
                        ? "%" + idNameDer
                        : this.derecha.getIr_ref().trim();

                if (refRealDer.startsWith("%%")) refRealDer = refRealDer.substring(1);

                resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n", ptrArrayDer, tipoEstructuraLLVM, refRealDer));
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