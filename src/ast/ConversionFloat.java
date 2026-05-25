package ast;

import llvm.CodeGeneratorHelper;

public class ConversionFloat extends Expresion {
    private final Expresion hijo;

    public ConversionFloat(Expresion hijo) {
        this.hijo = hijo;
    }

    @Override
    protected String getEtiqueta() {
        return "(INT_A_FLOAT)";
    }

    @Override
    public String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder dot = new StringBuilder();

        // Grafica este nodo (INT_A_FLOAT)
        dot.append(super.graficar(idPadre));

        // Grafica la conexión con el hijo (el ID original)
        if (hijo != null) {
            dot.append(hijo.graficar(miId));
        }

        return dot.toString();
    }
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Forzamos a que el nodo hijo genere su código primero (ej: si es una operación o una carga)
        if (this.hijo != null) {
            resultado.append(this.hijo.generarCodigo());
        }

        // 2. Pedimos un nuevo puntero temporal para guardar el resultado del casting (%ptro.X)
        this.setIr_ref(CodeGeneratorHelper.getNewPointer());

        // 3. Emitimos la instrucción 'sitofp' apuntando estrictamente 'to double'
        // LLVM exige: %nuevo_ptr = sitofp i32 %ptr_hijo to double
        resultado.append(String.format("  %1$s = sitofp i32 %2$s to double\n",
                this.getIr_ref(),
                this.hijo != null ? this.hijo.getIr_ref() : "0"
        ));

        return resultado.toString();
    }
}