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
        if (this.tipo.matches("\\d+")) return "ARRAY[" + this.tipo + "]";
        return switch (this.tipo) {
            case "i32" -> "INT";
            case "float", "double" -> "FLOAT";
            case "i1" -> "BOOL";
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

        // Guardamos el tipo ANTES de generar código de los hijos,
        // porque IdLiteral pisa su propio tipo al generar su código.
        String tipoIzqOriginal = this.izquierda != null ? this.izquierda.getTipo() : null;
        String tipoDerOriginal = this.derecha != null ? this.derecha.getTipo() : null;

        if (this.izquierda != null) resultado.append(this.izquierda.generarCodigo());
        if (this.derecha != null) resultado.append(this.derecha.generarCodigo());

        boolean esOperacionDeArreglo = this.tipo != null && this.tipo.matches("\\d+");

        if (esOperacionDeArreglo) {
            String opCode = this.get_llvm_op_code();

            // NUEVO: si un lado es escalar, lo convertimos a un array real en memoria
            String ptrIzq = this.izquierda.getIr_ref();
            String ptrDer = this.derecha.getIr_ref();

            boolean izqEsArreglo = tipoIzqOriginal != null &&
                    (tipoIzqOriginal.contains("ARRAY") || tipoIzqOriginal.matches("\\d+") || tipoIzqOriginal.contains("x double"));
            boolean derEsArreglo = tipoDerOriginal != null &&
                    (tipoDerOriginal.contains("ARRAY") || tipoDerOriginal.matches("\\d+") || tipoDerOriginal.contains("x double"));

            if (!izqEsArreglo) {
                ptrIzq = broadcastEscalar(this.izquierda, tipoIzqOriginal, this.tipo, resultado);
            }
            if (!derEsArreglo) {
                ptrDer = broadcastEscalar(this.derecha, tipoDerOriginal, this.tipo, resultado);
            }

            if ("5".equals(opCode) || "6".equals(opCode)) {
                this.setIr_ref(CodeGeneratorHelper.getNewPointer());
                resultado.append("; --- Invocación a comparar_arreglos ---\n");
                resultado.append(String.format(
                        "  %1$s = call i32 @comparar_arreglos(ptr %2$s, ptr %3$s, i32 %4$s, i32 %5$s)\n",
                        this.getIr_ref(), ptrIzq, ptrDer, this.tipo, opCode));
                String boolReg = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format(
                        "  %1$s = icmp ne i32 %2$s, 0\n",
                        boolReg, this.getIr_ref()));
                this.setIr_ref(boolReg);
            } else {
                String ptrDestino = CodeGeneratorHelper.getNewPointer();
                resultado.append(String.format(
                        "  %1$s = alloca [%2$s x double]\n", ptrDestino, this.tipo));
                resultado.append("; --- Invocación a la ALU de Arreglos ---\n");
                resultado.append(String.format(
                        "  call void @operar_arreglos(ptr %1$s, ptr %2$s, ptr %3$s, i32 %4$s, i32 %5$s)\n",
                        ptrIzq, ptrDer, ptrDestino, this.tipo, opCode));
                this.setIr_ref(ptrDestino);
            }
        } else {
            resultado.append(this.obtenerCodigoEscalar());
        }

        return resultado.toString();
    }

    // NUEVO: convierte un valor escalar (ya generado, en 'ir_ref') en un array de N doubles
    private String broadcastEscalar(Expresion escalar, String tipoEscalar, String tamano, StringBuilder resultado) {
        int n = Integer.parseInt(tamano);
        String valor = escalar.getIr_ref();

        // Si es entero, lo pasamos a double
        if ("INT".equalsIgnoreCase(tipoEscalar) || "i32".equals(tipoEscalar)) {
            String conv = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("  %1$s = sitofp i32 %2$s to double\n", conv, valor));
            valor = conv;
        }

        String ptrArr = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = alloca [%2$d x double]\n", ptrArr, n));
        for (int i = 0; i < n; i++) {
            String ptrElem = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format(
                    "  %1$s = getelementptr [%2$d x double], ptr %3$s, i64 0, i64 %4$d\n",
                    ptrElem, n, ptrArr, i));
            resultado.append(String.format("  store double %1$s, ptr %2$s\n", valor, ptrElem));
        }
        return ptrArr;
    }
}