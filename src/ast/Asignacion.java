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
        else if ("FLOAT_ARRAY".equals(tabla.getTipo(id))) {
            tipoLLVM = tabla.getTamano(id);
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

        String tipoDestino = tabla.getTipo(id);
        String tipoNormalizado = tipoDestino != null ? tipoDestino.toUpperCase() : "";

        String nombreVar = this.getNombreP();
        if (!nombreVar.startsWith("%")) {
            nombreVar = "%" + nombreVar;
        }

        // 🌟 SI EL DESTINO ES UN ARREGLO
        if (tipoNormalizado.contains("ARRAY")) {
            String tamano = tabla.getTamano(id);
            String tipoEstructuraLLVM = "[" + tamano + " x double]";

            // 1. Obtenemos el puntero a la celda 0 de la variable destino real (ej: %sum)
            String ptrDestinoReal = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n",
                    ptrDestinoReal, tipoEstructuraLLVM, nombreVar));

            // 2. Si es una operación binaria, delegamos el código pasándole el destino directo
            if (this.valor instanceof ast.OperacionBinaria) {
                resultado.append(((ast.OperacionBinaria) this.valor).generarCodigoConDestino(ptrDestinoReal));
            } else {
                // Asignación simple de arreglos si correspondiera (ej: sum = mediciones)
                if (this.valor != null) resultado.append(this.valor.generarCodigo());
            }
            return resultado.toString();
        }

        // 🌟 CAMINO ESCALAR TRADICIONAL
        if (this.valor != null) {
            resultado.append(this.valor.generarCodigo());
        }

        switch (tipoNormalizado) {
            case "INT", "I32" -> resultado.append(String.format("  store i32 %1$s, ptr %2$s\n", this.valor.getIr_ref(), nombreVar));
            case "FLOAT" -> resultado.append(String.format("  store double %1$s, ptr %2$s\n", this.valor.getIr_ref(), nombreVar));
            case "BOOLEAN", "I1", "BOOL" -> resultado.append(String.format("  store i1 %1$s, ptr %2$s\n", this.valor.getIr_ref(), nombreVar));
        }

        return resultado.toString();
    }
}