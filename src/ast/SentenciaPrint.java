package ast;

import llvm.CodeGeneratorHelper;

public class SentenciaPrint extends Sentencia {
    private final Nodo contenido;
    private String tipo; // Recibe directamente: "i32", "float", "i1", "i8*", etc.
    private final String tamano;  // ← nuevo campo

    public SentenciaPrint(Nodo contenido, String tipo, String tamano) {
        this.contenido = contenido;
        this.tipo = tipo;
        this.tamano = tamano;
    }

    // constructor de compatibilidad para el PRINT de strings que no cambia
    public SentenciaPrint(Nodo contenido, String tipo) {
        this(contenido, tipo, "0");
    }

    @Override
    protected String getNombreSentencia() { return "PRINT"; }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        return super.graficar(idPadre) +
                (contenido != null ? contenido.graficar(miId) : "");
    }
    private String tipoLenguaje() {
        if (this.tipo == null) return "?";
        return switch (this.tipo) {
            case "INT"                  -> "i32";
            case "FLOAT"    -> "float";
            case "BOOLEAN", "BOOL"       -> "i1";
            case "FLOAT_ARRAY", "ARRAY"        -> "ARRAY";
            default -> {
                if (this.tipo.matches("\\d+")) yield "ARRAY[" + this.tipo + "]";
                yield this.tipo;
            }
        };
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        if (this.contenido == null) return "";

        // Resolvemos el tipo del contenido de manera segura
        String tipoMapeado = tipoLenguaje();
        String tipoRealHijo = "";
        if (this.contenido instanceof Expresion) {
            tipoRealHijo = ((Expresion) this.contenido).getTipo();
        }

        // CASO 1: string literal
        if (this.contenido instanceof StringLiteral || "i8*".equals(this.tipo) || (this.tipo != null && this.tipo.startsWith("i8"))) {
            StringLiteral strLit = (StringLiteral) this.contenido;
            String declaracionGlobal = strLit.generarCodigo();
            CodeGeneratorHelper.agregarConstanteGlobal(declaracionGlobal);
            int longitud = strLit.getLongitudStr();
            String tempCall = CodeGeneratorHelper.getNewPointer();
            // Usa sintaxis 'ptr' moderna, consistente con el resto del backend.
            // getLongitudStr() ya cuenta los bytes UTF-8 + \0A + \00.
            resultado.append(String.format(
                    "  %1$s = call i32 (i8*, ...) @printf(ptr getelementptr ([%2$d x i8], [%2$d x i8]* %3$s, i32 0, i32 0))\n",
                    tempCall, longitud, strLit.getIr_ref()));
            return resultado.toString();
        }

        // CASO 2: array completo
        // 🌟 CORRECCIÓN CLAVE: Solo entramos acá si el tamaño es diferente de "0" y no es null.
        // Si es "0", sabemos que es un print(arreglo[indice]).
        boolean esArregloCompleto = (tipoMapeado.contains("ARRAY") || "array".equalsIgnoreCase(this.tipo))
                && this.tamano != null && !this.tamano.equals("0");

        if (esArregloCompleto) {
            Expresion exp = (Expresion) this.contenido;
            int n = Integer.parseInt(this.tamano);
            String nombreVar = ((ast.literal.IdLiteral) exp).getNombre();

            // imprimir "["
            String tempIni = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("  %s = call i32 (i8*, ...) @printf(ptr @.array_ini)\n", tempIni));

            // imprimir cada elemento
            for (int i = 0; i < n; i++) {
                String idxReg  = CodeGeneratorHelper.getNewPointer();
                String idx64   = CodeGeneratorHelper.getNewPointer();
                String gepReg  = CodeGeneratorHelper.getNewPointer();
                String valReg  = CodeGeneratorHelper.getNewPointer();
                String callReg = CodeGeneratorHelper.getNewPointer();

                resultado.append(String.format("  %s = add i32 0, %d\n", idxReg, i));
                resultado.append(String.format("  %s = sext i32 %s to i64\n", idx64, idxReg));
                resultado.append(String.format(
                        "  %s = getelementptr inbounds [%d x double], ptr %%%s, i64 0, i64 %s\n",
                        gepReg, n, nombreVar, idx64));
                resultado.append(String.format("  %s = load double, ptr %s\n", valReg, gepReg));
                resultado.append(String.format("  %s = call i32 (i8*, ...) @printf(ptr @.array_elem, double %s)\n", callReg, valReg));
            }

            // imprimir "]"
            String tempFin = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("  %s = call i32 (i8*, ...) @printf(ptr @.array_fin)\n", tempFin));

            return resultado.toString();
        }

        // CASO 3: Escalares (i32, double/float, i1) y Celdas de Arreglos (tamano == "0")
        Expresion exp = (Expresion) this.contenido;

        // Ejecutamos el código de la expresión (inyecta getelementptr y load del AccesoArray)
        resultado.append(exp.generarCodigo());
        String tempCall = CodeGeneratorHelper.getNewPointer();

        // Identificamos tipos
        boolean esInt = "i32".equals(tipoMapeado) || "INT".equalsIgnoreCase(this.tipo) || "INT".equalsIgnoreCase(tipoRealHijo);

        // 🌟 MAGIA: Si el tipo incluye ARRAY y llegó hasta acá, es SÍ O SÍ una celda flotante. Lo forzamos a Float.
        boolean esFloat = "float".equals(tipoMapeado) || "double".equals(tipoMapeado) ||
                "FLOAT".equalsIgnoreCase(this.tipo) || "FLOAT".equalsIgnoreCase(tipoRealHijo) ||
                tipoMapeado.contains("ARRAY") || tipoRealHijo.contains("ARRAY") || "array".equalsIgnoreCase(this.tipo);

        boolean esBool = "i1".equals(tipoMapeado) || "BOOLEAN".equalsIgnoreCase(this.tipo) || "BOOLEAN".equalsIgnoreCase(tipoRealHijo);

        if (esInt) {
            resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.integer, i32 0, i32 0), i32 %s)\n",
                    tempCall, exp.getIr_ref()));

        } else if (esFloat) {
            // Imprime incondicionalmente como double (tu arreglo en LLVM es double)
            resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.float, i32 0, i32 0), double %s)\n",
                    tempCall, exp.getIr_ref()));

        } else if (esBool) {
            String registerInt = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("  %s = zext i1 %s to i32\n", registerInt, exp.getIr_ref()));
            resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.integer, i32 0, i32 0), i32 %s)\n",
                    tempCall, registerInt));
        } else {
            // Si por algún milagro oscuro no detecta el tipo, te chismosea en el archivo .ll en lugar de desaparecer
            resultado.append(String.format(
                    "  ; [DEBUG ALERTA] Fallo al imprimir. tipoMapeado='%s', tipoRealHijo='%s'\n",
                    tipoMapeado, tipoRealHijo));
        }

        return resultado.toString();
    }
}