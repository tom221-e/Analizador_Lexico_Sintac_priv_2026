package ast;

import ast.literal.IdLiteral;
import llvm.CodeGeneratorHelper;
import parser.SymbolTable;

public class Asignacion extends Sentencia {
    private final String id;
    private final Expresion valor;
    private final SymbolTable tabla;

    public Asignacion(String id, Expresion valor, SymbolTable tabla) {
        this.id = id;
        this.valor = valor;
        this.tabla = tabla;
    }

    @Override
    protected String getNombreSentencia() {
        return "="; // El globo del grafo mostrará el nombre de la variable
    }

    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder grafico = new StringBuilder();

        // 1. Graficamos el nodo actual (el "=")
        grafico.append(super.graficar(idPadre));

        // 2. Graficamos el lado IZQUIERDO (el ID)
        // Creamos un IdLiteral temporal para que genere su propio globo
        IdLiteral nodoId = new IdLiteral(id, tabla);
        grafico.append(nodoId.graficar(miId));

        // 3. Graficamos el lado DERECHO (el valor o expresión)
        grafico.append(valor.graficar(miId));

        return grafico.toString();
    }
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código de la expresión de la derecha
        resultado.append(this.valor.generarCodigo());

        // 2. Obtenemos el tipo desde tu tabla de símbolos
        String tipoDeMiVariable = tabla.getTipo(this.id);

        // 3. Generamos el store con el símbolo % incluido
        if ("INT".equals(tipoDeMiVariable)) {
            // El %%%2$s genera un '%' seguido del nombre de la variable
            resultado.append(String.format("store i32 %1$s, i32* %2$s\n",
                    this.valor.getIr_ref(), this.id));

        } else if ("FLOAT".equals(tipoDeMiVariable)) {
            resultado.append(String.format("store float %1$s, float* %2$s\n",
                    this.valor.getIr_ref(), this.id));

        } else if ("BOOLEAN".equals(tipoDeMiVariable)) {
            // En LLVM los booleanos son i1
            resultado.append(String.format("store i1 %1$s, i1* %2$s\n",
                    this.valor.getIr_ref(), this.id));
        }

        return resultado.toString();
    }
}