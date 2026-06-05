package ast.unario;

import ast.Expresion;
import ast.OperacionUnaria;
import llvm.CodeGeneratorHelper;

public class MenosUnario extends OperacionUnaria {

    // 🌟 SOLUCCIÓN: Ya no necesitamos la variable 'tabla' para generar código.
    // Dejamos este constructor por si tu archivo CUP/Parser todavía le pasa la tabla,
    // así no te tira error de compilación allá.
    public MenosUnario(Expresion operando, parser.SymbolTable tabla) {
        super(operando);
    }

    // Constructor limpio por si en el Parser decidís dejar de pasarle la tabla
    public MenosUnario(Expresion operando) {
        super(operando);
    }

    @Override
    protected String getNombreOperacion() {
        return "MENOS_UNARIO";
    }

    @Override
    public Expresion getOperando() {
        return this.operando;
    }

    // 🌟 CLAVE: Sobreescribimos getTipo() para que si este nodo es hijo de otra
    // operación (ej: una suma), el padre sepa qué tipo de dato aporta este bloque.
    @Override
    public String getTipo() {
        return this.operando != null ? this.operando.getTipo() : "";
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código del operando
        resultado.append(this.operando.generarCodigo());

        // 🌟 NUEVA VALIDACIÓN DIRECTA: Le pedimos el tipo directamente al operando
        String tipoOperando = this.operando.getTipo();

        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // Evaluamos si es entero comparando con tu token "INT" o el tipo de LLVM "i32"
        if ("INT".equals(tipoOperando) || "i32".equals(tipoOperando)) {
            resultado.append(String.format("  %1$s = sub i32 0, %2$s\n",
                    this.getIr_ref(),
                    this.operando.getIr_ref()));
        } else {
            // Mantenemos tu corrección de double en lugar de float
            resultado.append(String.format("  %1$s = fsub double 0.0, %2$s\n",
                    this.getIr_ref(),
                    this.operando.getIr_ref()));
        }

        return resultado.toString();
    }
}