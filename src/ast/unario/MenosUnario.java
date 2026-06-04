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
    public Expresion getOperando() {
        return this.operando;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        resultado.append(this.operando.generarCodigo());

        // instanciar SIN tabla — usar la tabla como parámetro igual que antes
        ValidatorDataType validator = new ValidatorDataType();
        ValidatorDataType.InfoNodo info = validator.obtenerInfo(this.operando, this.tabla);

        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        if ("INT".equals(info.getTipo())) {
            resultado.append(String.format("  %1$s = sub i32 0, %2$s\n",
                    this.getIr_ref(),
                    this.operando.getIr_ref()));
        } else {
            // corrección: double en lugar de float
            resultado.append(String.format("  %1$s = fsub double 0.0, %2$s\n",
                    this.getIr_ref(),
                    this.operando.getIr_ref()));
        }

        return resultado.toString();
    }
}