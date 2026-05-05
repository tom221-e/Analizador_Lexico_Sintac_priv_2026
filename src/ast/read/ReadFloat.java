package ast.read;

import ast.Expresion;

public class ReadFloat extends Expresion {

    public ReadFloat() {
        // No requiere lógica en el constructor
    }

    @Override
    protected String getEtiqueta() {
        // Esta es la etiqueta exacta que pediste
        return "READ: Float";
    }

    @Override
    protected String graficar(String idPadre) {
        // Llama al graficador de la superclase (Nodo/Expresion)
        // para crear el nodo con la etiqueta "READ: INT" y conectarlo
        return super.graficar(idPadre);
    }
}