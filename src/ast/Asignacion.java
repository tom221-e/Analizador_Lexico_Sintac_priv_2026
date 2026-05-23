package ast;

import ast.literal.IdLiteral;
import llvm.CodeGeneratorHelper;
import parser.SymbolTable;

public class Asignacion extends Sentencia {
    private final String id;
    private final Expresion valor;
    private final String tipo;
    private  final SymbolTable tabla;


    public Asignacion(String id, Expresion valor, SymbolTable tabla, String tipo) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
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
        String tipoLLVM="";
        if ("INT".equals(tabla.getTipo(id))) {
            tipoLLVM = "i32";
        } else if ("FLOAT".equals(tabla.getTipo(id))) {
            tipoLLVM = "float";
        } else if ("BOOLEAN".equals(tabla.getTipo(id))) {
            tipoLLVM = "i1";
        }
        IdLiteral nodoId = new IdLiteral(id, tipoLLVM);
        grafico.append(nodoId.graficar(miId));

        // 3. Graficamos el lado DERECHO (el valor o expresión)
        grafico.append(valor.graficar(miId));

        return grafico.toString();
    }
    protected String getNombreP() {
        return "%"+id; // El globo del grafo mostrará el nombre de la variable
    }
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. Generamos el código de la expresión de la derecha
        resultado.append(this.valor.generarCodigo());

        // 2. Generamos el store con el símbolo % incluido
        if ("INT".equals(this.tipo)) {
            // El %%%2$s genera un '%' seguido del nombre de la variable
            resultado.append(String.format("store i32 %1$s, i32* %2$s\n",
                    this.valor.getIr_ref(), this.getNombreP()));

        } else if ("FLOAT".equals(this.tipo)) {
            resultado.append(String.format("store float %1$s, float* %2$s\n",
                    this.valor.getIr_ref(), this.getNombreP()));

        } else if ("BOOLEAN".equals(this.tipo)) {
            // En LLVM los booleanos son i1
            resultado.append(String.format("store i1 %1$s, i1* %2$s\n",
                    this.valor.getIr_ref(), this.getNombreP()));
        }

        return resultado.toString();
    }
}