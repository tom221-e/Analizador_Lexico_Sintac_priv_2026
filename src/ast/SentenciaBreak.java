package ast;

import llvm.CodeGeneratorHelper;

public class SentenciaBreak extends Sentencia {

    @Override
    protected String getNombreSentencia() {
        return "BREAK";
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Obtenemos la etiqueta de salida del bucle contenedor actual
        String tagDestino = CodeGeneratorHelper.getCurrentBreakTag();

        if (tagDestino != null) {
            // 2. Generamos el salto incondicional de LLVM hacia esa salida
            resultado.append(String.format("br label %1$s\n\n", tagDestino));
        } else {
            // Por si el usuario escribe un break fuera de un ciclo (Error semántico)
            resultado.append("; ERROR: Se encontró un 'break' fuera de un bucle activo\n");
        }

        return resultado.toString();
    }
}