package ast.unario;

import ast.Expresion;
import ast.OperacionUnaria;
import parser.SymbolTable;
import validator.ValidatorDataType;
import llvm.CodeGeneratorHelper;

public class MenosUnario extends OperacionUnaria {
    private final SymbolTable tabla; // Necesaria para el validador

    public MenosUnario(Expresion operando, SymbolTable tabla) {
        super(operando);
        this.tabla = tabla;
    }

    @Override
    protected String getNombreOperacion() {
        return "MENOS_UNARIO";
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código del operando primero (recursividad)
        resultado.append(this.operando.generarCodigo());

        // 2. Usamos el validador para determinar si es INT o FLOAT
        ValidatorDataType validator = new ValidatorDataType();
        ValidatorDataType.InfoNodo info = validator.obtenerInfo(this.operando, this.tabla);

        // 3. Solicitamos un nuevo registro para el resultado
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        if ("INT".equals(info.getTipo())) {
            // Para enteros: 0 - operando
            resultado.append(String.format("%1$s = sub i32 0, %2$s\n",
                    this.getIr_ref(),
                    this.operando.getIr_ref()));
        } else {
            // Para flotantes: 0.0 - operando
            resultado.append(String.format("%1$s = fsub float 0.0, %2$s\n",
                    this.getIr_ref(),
                    this.operando.getIr_ref()));
        }

        return resultado.toString();
    }
}