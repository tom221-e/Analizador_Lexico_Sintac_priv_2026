package ast;

import llvm.CodeGeneratorHelper;

public class AccesoArray extends Expresion {
    private final String nombre;
    private final Expresion indice;
    private final String tamano;

    public AccesoArray(String nombre, Expresion indice, String tamano) {
        this.nombre = nombre;
        this.indice = indice;
        this.tamano = tamano;
    }

    /*@Override
    protected String getEtiqueta() {
        return "ACCESO ARRAY: " + nombre + "[" + indice.getEtiqueta() + "]";
    }*/
    @Override
    protected String getEtiqueta() {
        return "FLOAT: " + nombre + "[" + indice.getEtiqueta() + "]";
    }

    @Override
    public String getId() {
        return nombre;
    }

    @Override
    protected String graficar(String idPadre) {
        String idVisual = "acceso_" + System.identityHashCode(this);
        StringBuilder dot = new StringBuilder();

        dot.append(String.format("%s [label=\"%s\"];\n", idVisual, getEtiqueta()));
        dot.append(String.format("%s -> %s;\n", idPadre, idVisual));

        return dot.toString();
    }

    protected String getNombreP() {
        return "%" + nombre;
    }

    @Override
    public String getTipo() {
        return "FLOAT";  // los elementos de un array siempre son double/FLOAT
    }
    
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. generar código del índice
        resultado.append(this.indice.generarCodigo());

        // 2. extender índice a i64
        String ptrIndice64 = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = sext i32 %2$s to i64\n",
                ptrIndice64,
                this.indice.getIr_ref()
        ));

        // === CHEQUEO DE BOUNDS ===

        // verificar índice >= 0
        String checkNeg = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %s = icmp sge i32 %s, 0\n",
                checkNeg, this.indice.getIr_ref()));

        // verificar índice < tamaño
        String checkMax = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %s = icmp slt i32 %s, %s\n",
                checkMax, this.indice.getIr_ref(), this.tamano));

        // ambas condiciones deben ser true
        String checkOk = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %s = and i1 %s, %s\n",
                checkOk, checkNeg, checkMax));

        // bifurcar según resultado
        String labelOk    = CodeGeneratorHelper.getNewTag();
        String labelError = CodeGeneratorHelper.getNewTag();
        resultado.append(String.format("  br i1 %s, label %%%s, label %%%s\n\n",
                checkOk, labelOk, labelError));

        // bloque error
        resultado.append(labelError).append(":\n");
        String msgReg  = CodeGeneratorHelper.getNewPointer();
        String callReg = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format(
                "  %s = call i32 (i8*, ...) @printf(ptr @.bounds_error)\n", msgReg));
        resultado.append(String.format(
                "  %s = call i32 @exit(i32 1)\n", callReg));
        resultado.append("  unreachable\n\n");

        // bloque acceso válido
        resultado.append(labelOk).append(":\n");

        // === FIN CHEQUEO DE BOUNDS ===

        // 3. calcular dirección del elemento
        String ptrDireccion = CodeGeneratorHelper.getNewPointer();
        String tipoEstructura = "[" + this.tamano + " x double]";
        resultado.append(String.format("  %1$s = getelementptr inbounds %2$s, ptr %3$s, i64 0, i64 %4$s\n",
                ptrDireccion,
                tipoEstructura,
                this.getNombreP(),
                ptrIndice64
        ));

        // 4. cargar el valor
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());
        resultado.append(String.format("  %1$s = load double, ptr %2$s\n",
                this.getIr_ref(),
                ptrDireccion
        ));

        return resultado.toString();
    }
    
}