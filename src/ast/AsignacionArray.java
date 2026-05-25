package ast;

import ast.literal.IdLiteral;
import llvm.CodeGeneratorHelper; // Aseguramos la importación del helper

public class AsignacionArray extends Sentencia {
    private final String nombre, tamano;
    private final Expresion indice;
    private final Expresion valor;

    public AsignacionArray(String nombre, Expresion indice, Expresion valor, String tamano) {
        this.nombre = nombre;
        this.indice = indice;
        this.valor = valor;
        this.tamano = tamano;
    }

    @Override
    protected String getNombreSentencia() {
        return "=";
    }

    // NO SE TOCÓ EL GRAFICADOR (Solo se agregó @Override por buena práctica)
    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder grafico = new StringBuilder();

        // 1. Graficamos el nodo actual (el "=")
        grafico.append(super.graficar(idPadre));

        // 2. Graficamos el lado IZQUIERDO (el ID)
        IdLiteral nodoId = new IdLiteral(nombre, "FLOAT_ARRAY");
        grafico.append(nodoId.graficar(miId));

        // 3. Graficamos el lado DERECHO (el valor o expresión)
        grafico.append(valor.graficar(miId));

        return grafico.toString();
    }

    // Método auxiliar para obtener el puntero formateado de LLVM para esta variable (%nombre)
    protected String getNombreP() {
        return "%" + nombre;
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código para calcular el índice y el valor
        resultado.append(this.indice.generarCodigo());
        resultado.append(this.valor.generarCodigo());

        // 2. Solicitamos el puntero temporal
        String ptrElemento = CodeGeneratorHelper.getNewPointer();

        // 3. getelementptr inbounds para arrays de floats
        // NOTA: Se usa el método local 'this.getNombreP()' para inyectar correctamente el '%nombre'
        resultado.append(String.format("%1$s = getelementptr inbounds [%2$s x float], [%2$s x float]* %3$s, i32 0, i32 %4$s\n",
                ptrElemento,
                this.tamano,
                this.getNombreP(),
                this.indice.getIr_ref()
        ));

        // 4. Guardamos el valor calculado en la posición indexada
        resultado.append(String.format("store double %1$s, float* %2$s\n",
                this.valor.getIr_ref(),
                ptrElemento
        ));

        return resultado.toString();
    }
}