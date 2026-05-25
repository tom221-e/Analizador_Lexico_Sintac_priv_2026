package ast.literal;

import ast.Expresion;
import llvm.CodeGeneratorHelper;
import parser.SymbolTable;
import validator.ValidatorDataType; // Importamos tu validador

public class IdLiteral extends Expresion {
    private final String valor;
    private String tipo;

    public IdLiteral(String valor, String tipo) {
        this.valor = String.valueOf(valor);
        this.tipo = tipo;
    }

    public String getNombreVariable() {
        return valor;
    }

    @Override
    protected String getEtiqueta() {
        return "ID(" +tipo+"): " + valor;
    }

    public String getStringID() {
        return "%" + valor;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // Identificamos si es un tipo escalar conocido
        boolean esEscalar = "i32".equals(this.tipo) || "float".equals(this.tipo) || "i1".equals(this.tipo);

        if (esEscalar) {
            // === CASO ESCALAR ===
            // Necesitamos extraer el valor guardado en el puntero, generamos un temporal
            this.setIr_ref(CodeGeneratorHelper.getNewPointer());
            String tipo2 = this.tipo;
            if ("float".equals(this.tipo)){
                tipo2="double";
            }
            resultado.append(String.format("%1$s = load %2$s, %3$s* %4$s\n",
                    this.getIr_ref(),   // El nuevo temporal (%1)
                    tipo2,          // El tipo (i32, float, i1)
                    tipo2,
                    this.getStringID()  // El nombre de la variable original (%x)
            ));
        } else {
            // === CASO ARREGLO (Pasar el puntero directo) ===
            // Como el arreglo declarado por alloca ya es un puntero en sí mismo (%miArray),
            // su referencia IR es directamente el identificador de la variable.
            this.setIr_ref(this.getStringID());

            // No se genera ninguna línea de código de carga en el archivo .ll,
            // porque el puntero ya está disponible para usarse directamente.
            return "";
        }

        return resultado.toString();
    }
}