package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class OperacionOr extends OperacionBinaria {

    public OperacionOr(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "OR"; // Token visual para el AST
    }
    
    @Override
    public String getTipo() {
        return "BOOLEAN";
    }

    @Override
    public String get_llvm_op_code() {
        return "or"; // Opcode nativo de LLVM para operaciones lógicas de bits/booleanos
    }

    /**
     * Genera la instrucción lógica escalar para el operador OR.
     * En LLVM, tanto los operandos de entrada como el de salida son de tipo 'i1' (1 bit).
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder res = new StringBuilder();

        // 1. Reservamos espacio para el resultado
        String ptrResultado = CodeGeneratorHelper.getNewPointer();
        res.append(String.format("  %s = alloca i1\n", ptrResultado));
        // Inicializamos con false por defecto
        res.append(String.format("  store i1 false, ptr %s\n", ptrResultado));

        // 2. Generar izquierda
        res.append(izquierda.generarCodigo());

        String tagDerecha = CodeGeneratorHelper.getNewTag();
        String tagFin = CodeGeneratorHelper.getNewTag();

        // 3. Salto condicional
        // Si izquierda es TRUE, saltamos a un bloque de éxito (etiquetaExito)
        // Si es FALSE, vamos a tagDerecha
        String etiquetaExito = CodeGeneratorHelper.getNewTag();
        res.append(String.format("  br i1 %s, label %%%s, label %%%s\n",
                izquierda.getIr_ref(), etiquetaExito, tagDerecha));

        // 4. Si es TRUE: guardamos true y saltamos al FIN
        res.append(etiquetaExito).append(":\n");
        res.append(String.format("  store i1 true, ptr %s\n", ptrResultado));
        res.append(String.format("  br label %%%s\n", tagFin));

        // 5. Si es FALSE: evaluamos derecha y guardamos
        res.append(tagDerecha).append(":\n");
        res.append(derecha.generarCodigo());
        res.append(String.format("  store i1 %s, ptr %s\n", derecha.getIr_ref(), ptrResultado));
        res.append(String.format("  br label %%%s\n", tagFin));

        // 6. Bloque FINAL: lectura
        res.append(tagFin).append(":\n");
        String finalReg = CodeGeneratorHelper.getNewPointer();
        this.setIr_ref(finalReg);
        res.append(String.format("  %s = load i1, ptr %s\n", finalReg, ptrResultado));

        return res.toString();
    }
}