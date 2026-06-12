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
        tipo=tipoLenguaje();

        if (this.contenido == null) return "";

        // CASO 1: string literal
        if (this.tipo.startsWith("i8") || this.contenido instanceof StringLiteral) {
            StringLiteral strLit = (StringLiteral) this.contenido;
            String declaracionGlobal = strLit.generarCodigo();
            CodeGeneratorHelper.agregarConstanteGlobal(declaracionGlobal);
            int longitud = strLit.getLongitudStr();
            String tempCall = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format(
                "  %1$s = call i32 (i8*, ...) @printf(i8* getelementptr ([%2$d x i8], [%2$d x i8]* %3$s, i32 0, i32 0))\n",
                tempCall, longitud, strLit.getIr_ref()));
            return resultado.toString();
        }

        // CASO 2: array
        if ("array".equals(this.tipo)) {
            Expresion exp = (Expresion) this.contenido;
            int n = Integer.parseInt(this.tamano);
            String nombreVar = ((ast.literal.IdLiteral) exp).getNombre();

            // imprimir "["
            String tempIni = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format(
                "  %s = call i32 (i8*, ...) @printf(ptr @.array_ini)\n", tempIni));

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
                resultado.append(String.format(
                    "  %s = load double, ptr %s\n", valReg, gepReg));
                resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(ptr @.array_elem, double %s)\n",
                    callReg, valReg));
            }

            // imprimir "]"
            String tempFin = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format(
                "  %s = call i32 (i8*, ...) @printf(ptr @.array_fin)\n", tempFin));

            return resultado.toString();
        }

        // CASO 3: escalares (i32, float, i1)
        Expresion exp = (Expresion) this.contenido;
        resultado.append(exp.generarCodigo());
        String tempCall = CodeGeneratorHelper.getNewPointer();

        // Modificamos las condiciones para que acepten variantes de nombres:
        if ("i32".equals(this.tipo) || "INT".equalsIgnoreCase(this.tipo)) {
            resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.integer, i32 0, i32 0), i32 %s)\n",
                    tempCall, exp.getIr_ref()));

        } else if ("float".equals(this.tipo) || "double".equals(this.tipo) || "FLOAT".equalsIgnoreCase(this.tipo)) {
            resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.float, i32 0, i32 0), double %s)\n",
                    tempCall, exp.getIr_ref()));


        } else if ("i1".equals(this.tipo) || "BOOLEAN".equalsIgnoreCase(this.tipo)) {
            String registerInt = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format(
                    "  %s = zext i1 %s to i32\n", registerInt, exp.getIr_ref()));
            resultado.append(String.format(
                    "  %s = call i32 (i8*, ...) @printf(i8* getelementptr ([4 x i8], [4 x i8]* @.integer, i32 0, i32 0), i32 %s)\n",
                    tempCall, registerInt));
        }

        return resultado.toString();
    }
}