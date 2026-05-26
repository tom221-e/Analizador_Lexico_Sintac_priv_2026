package ast;

import llvm.CodeGeneratorHelper;

public class SentenciaContinue extends Sentencia {

    @Override
    protected String getNombreSentencia() {
        return "CONTINUE";
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Obtenemos la etiqueta donde se evalúa la condición del ciclo actual
        String tagDestino = CodeGeneratorHelper.getCurrentContinueTag();

        if (tagDestino != null) {
            // 2. Generamos el salto incondicional de LLVM hacia la condición
            resultado.append(String.format("br label %1$s\n\n", "%"+tagDestino));
        } else {
            // Por si escriben un continue huérfano fuera de un bucle
            resultado.append("; ERROR: Se encontró un 'continue' fuera de un bucle activo\n");
        }

        return resultado.toString();
    }
}