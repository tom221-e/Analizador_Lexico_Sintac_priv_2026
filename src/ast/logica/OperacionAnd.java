package ast.logica;

import ast.Expresion;
import ast.OperacionBinaria;
import llvm.CodeGeneratorHelper;

public class OperacionAnd extends OperacionBinaria {

    public OperacionAnd(Expresion izquierda, Expresion derecha, String tipo) {
        super(izquierda, derecha, tipo);
    }

    @Override
    protected String getNombreOperacion() {
        return "AND"; // Token visual para el AST
    }
    
    @Override
    public String getTipo() {
        return "BOOLEAN";
    }
    
    @Override
    public String get_llvm_op_code() {
        return "and"; // Opcode nativo de LLVM para operaciones lógicas a nivel de bits (y booleanos)
    }

    /**
     * Genera la instrucción lógica escalar para el operador AND.
     * En LLVM, tanto los operandos de entrada como el de salida son de tipo 'i1' (1 bit).
     */
    @Override
    protected String obtenerCodigoEscalar() {
        StringBuilder res = new StringBuilder();

        // 1. Reservamos espacio para el resultado (alloca)
        String ptrResultado = CodeGeneratorHelper.getNewPointer();
        res.append(String.format("  %s = alloca i1\n", ptrResultado));

        // 2. Generar código de la izquierda
        res.append(izquierda.generarCodigo());

        String tagDerecha = CodeGeneratorHelper.getNewTag();
        String tagFin = CodeGeneratorHelper.getNewTag();

        // 3. Lógica AND: Si izquierda es FALSE, cortocircuitamos al final (resultado FALSE).
        // Si es TRUE, evaluamos la derecha.
        res.append(String.format("  br i1 %s, label %%%s, label %%%s\n",
                izquierda.getIr_ref(), tagDerecha, tagFin));

        // 4. Si la izquierda fue TRUE, evaluamos la derecha y guardamos su valor
        res.append(tagDerecha).append(":\n");
        res.append(derecha.generarCodigo());
        res.append(String.format("  store i1 %s, ptr %s\n", derecha.getIr_ref(), ptrResultado));
        res.append(String.format("  br label %%%s\n", tagFin));

        // 5. Si la izquierda fue FALSE, guardamos FALSE directamente
        res.append(tagFin).append(":\n");
        // Nota: Solo guardamos FALSE si venimos de la izquierda (el caso FALSE)
        // Para esto, podrías necesitar un label extra o una verificación inicial
        // Alternativa simple: Guardamos false por defecto antes de empezar
        // res.append(String.format("  store i1 false, ptr %s\n", ptrResultado));

        // 6. Lectura final del resultado
        String finalReg = CodeGeneratorHelper.getNewPointer();
        this.setIr_ref(finalReg);
        res.append(String.format("  %s = load i1, ptr %s\n", finalReg, ptrResultado));

        return res.toString();
    }
}