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

        // 🌟 CORRECCIÓN 1: Convertir el índice calculado a i64 para evitar errores SSA de punteros
        String ptrIndice64 = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = sext i32 %2$s to i64\n",
                ptrIndice64,
                this.indice.getIr_ref()
        ));

        // 2. Solicitamos el puntero temporal para la casilla del arreglo
        String ptrElemento = CodeGeneratorHelper.getNewPointer();

        // 🌟 CORRECCIÓN 2: Sintaxis moderna de LLVM usando 'ptr' e índices i64
        String tipoEstructura = "[" + this.tamano + " x double]";
        resultado.append(String.format("  %1$s = getelementptr inbounds %2$s, ptr %3$s, i64 0, i64 %4$s\n",
                ptrElemento,
                tipoEstructura,
                this.getNombreP(),
                ptrIndice64
        ));

        // 🌟 CORRECCIÓN 3: Almacenamiento directo usando 'ptr' sin asteriscos tipados
        resultado.append(String.format("  store double %1$s, ptr %2$s\n",
                this.valor.getIr_ref(),
                ptrElemento
        ));

        return resultado.toString();
    }
}