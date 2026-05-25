package ast;

import llvm.CodeGeneratorHelper;

public class StringLiteral extends Expresion {
    private final String valor;
    private int tamanoRealBytes = 0;

    public StringLiteral(String valor) {
        this.valor = valor.replace("\"", "");
    }

    @Override
    protected String getEtiqueta() {
        return "STRING: " + valor;
    }

    @Override
    protected String graficar(String idPadre) {
        return super.graficar(idPadre);
    }

    public String getStr() {
        return valor;
    }

    public Integer getLongitudStr() {
        if (this.tamanoRealBytes == 0) {
            calcularTamanoLLVM(); // Por si acaso se llama antes de generarCodigo
        }
        return this.tamanoRealBytes;
    }

    private String calcularTamanoLLVM() {
        String valorEscapado = this.valor.replace("\r\n", "\\0A")
                .replace("\n", "\\0A")
                .replace("\r", "\\0D");

        int numEscapes = 0;
        int index = valorEscapado.indexOf("\\");
        while (index != -1) {
            numEscapes++;
            index = valorEscapado.indexOf("\\", index + 1);
        }

        // Almacenamos el tamaño exacto que LLVM interpretará en bytes
        this.tamanoRealBytes = valorEscapado.length() - (numEscapes * 2) + 1;
        return valorEscapado;
    }

    @Override
    public String generarCodigo() {
        String refGlobal = CodeGeneratorHelper.getNewPointer().replace("%", "@");
        this.setIr_ref(refGlobal);

        // Calculamos el valor escapado y congelamos el 'tamanoRealBytes'
        String valorEscapado = calcularTamanoLLVM();

        return String.format("%1$s = private unnamed_addr constant [%2$d x i8] c\"%3$s\\00\"\n",
                this.getIr_ref(),
                this.tamanoRealBytes,
                valorEscapado
        );
    }
}