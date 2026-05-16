package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;
import parser.SymbolTable;
import validator.ValidatorDataType; // Importamos tu validador

public class IdLiteral extends Expresion {
    private final String valor;
    private final SymbolTable symbolTable;

    public IdLiteral(String valor, SymbolTable table) {
        this.valor = String.valueOf(valor);
        this.symbolTable = table;
    }

    public String getNombreVariable() {
        return valor;
    }

    @Override
    protected String getEtiqueta() {
        return "ID: " + valor;
    }

    public String getStringID() {
        return "%" + valor;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Usamos tu validador para obtener la información de la variable
        ValidatorDataType validator = new ValidatorDataType();
        ValidatorDataType.InfoNodo info = validator.obtenerInfo(this, symbolTable);

        // 2. Mapeamos el tipo de tu lenguaje al tipo de LLVM
        String tipoLLVM = "";
        String tipoOriginal = info.getTipo(); // Asumiendo que devuelve "INT", "FLOAT", etc.

        if ("INT".equals(tipoOriginal)) {
            tipoLLVM = "i32";
        } else if ("FLOAT".equals(tipoOriginal)) {
            tipoLLVM = "float";
        } else if ("BOOLEAN".equals(tipoOriginal)) {
            tipoLLVM = "i1";
        } else {
            return "; ERROR: Tipo desconocido para la variable " + valor + "\n";
        }

        // 3. Pedimos un nuevo puntero temporal para el resultado del load
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 4. Generamos la instrucción load con el tipo correcto
        // Ejemplo: %1 = load i32, i32* %x
        resultado.append(String.format("%1$s = load %2$s, %2$s* %3$s\n",
                this.getIr_ref(),   // El nuevo temporal (%1)
                tipoLLVM,           // El tipo (i32, float, i1)
                this.getStringID()  // El nombre de la variable original (%x)
        ));

        return resultado.toString();
    }
}

}