package ast;

import llvm.CodeGeneratorHelper;

import java.nio.charset.Charset;

public class StringLiteral extends Expresion {
    private final String valor;
    private int tamanoRealBytes = 0;
    private String contenidoLLVM = null;

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

    /**
     * Convierte el string Java a la representación LLVM byte a byte en CP850 para la Ñ, UTF-8 para unicide completo pero la ñ no la imprime.
     */

    private void calcularTamanoLLVM() {
        // Codificar en Latin-1: cada carácter del rango 0x00–0xFF queda en un solo byte.
        // Caracteres fuera de Latin-1 (p.ej. emojis) se reemplazan por '?' automáticamente.
        Charset latin1 = Charset.forName("UTF-8"); // se cambia utf16 por 8
        byte[] bytes = this.valor.getBytes(latin1);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int unsigned = b & 0xFF;
            if (unsigned >= 0x20 && unsigned <= 0x7E
                    && unsigned != '"'
                    && unsigned != '\\') {
                // Byte ASCII imprimible seguro: emitir directamente
                sb.append((char) unsigned);
            } else {
                // Byte de control o Latin-1 extendido (ñ, á, é, etc.): escapar como \XX
                sb.append(String.format("\\%02X", unsigned));
            }
        }

        // Agregar salto de línea final antes del terminador nulo
        sb.append("\\0A");

        this.contenidoLLVM = sb.toString();

        // Tamaño = bytes Latin-1 del contenido + 1 (newline) + 1 (null terminator)
        this.tamanoRealBytes = bytes.length + 2;
    }

    @Override
    public String generarCodigo() {
        String refGlobal = CodeGeneratorHelper.getNewPointer().replace("%", "@");
        this.setIr_ref(refGlobal);

        if (this.contenidoLLVM == null) {
            calcularTamanoLLVM();
        }

        // La constante global incluye el contenido + \0A + \00
        return String.format("%1$s = private unnamed_addr constant [%2$d x i8] c\"%3$s\\00\"\n",
                this.getIr_ref(),
                this.tamanoRealBytes,
                this.contenidoLLVM
        );
    }
}