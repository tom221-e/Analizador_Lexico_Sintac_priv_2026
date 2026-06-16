package ast;

import ast.literal.IdLiteral;
import llvm.CodeGeneratorHelper;

public class Asignacion extends Sentencia {
    private final String id;
    private final Expresion valor;
    private final String tipo;       // Tipo resultante de la expresión derecha (ej: "INT", "FLOAT", "10")
    private final String idTipo;     // Tipo original con el que nació la variable izquierda (ej: "INT", "FLOAT_ARRAY")


    public Asignacion(String id, Expresion valor, String idTipo, String tipo) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.idTipo = idTipo;
    }

    @Override
    protected String getNombreSentencia() {
        return "="; // El globo del grafo mostrará el operador de asignación
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        StringBuilder grafico = new StringBuilder();

        // 1. Graficamos el nodo actual (el "=")
        grafico.append(super.graficar(idPadre));

        // 2. Determinar el tipo a graficar según la variable 'this.tipo'
        String tipoAGraficar = "";

        if ("INT".equals(this.idTipo) || "FLOAT".equals(this.idTipo) || "BOOLEAN".equals(this.idTipo)) {
            tipoAGraficar = this.idTipo;
        } else {
            // Si no es ninguno de los anteriores, asumimos que es un arreglo
            // y que 'this.tipo' contiene la dimensión numérica (ej: "10", "50")
            tipoAGraficar = "FLOAT_ARRAY";
        }

        // 3. Graficamos el lado IZQUIERDO (el ID de la variable con su tipo corregido)
        IdLiteral nodoId = new IdLiteral(id, tipoAGraficar);
        grafico.append(nodoId.graficar(miId));

        // 4. Graficamos el lado DERECHO (la expresión o valor)
        grafico.append(valor.graficar(miId));

        return grafico.toString();
    }

    protected String getNombreP() {
        return "%" + id; // El nombre para los registros en LLVM
    }

    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 🌟 CORREGIDO: Ahora usamos 'idTipo' en lugar de ir a buscarlo a la tabla
        String tipoNormalizado = idTipo != null ? idTipo.toUpperCase() : "";

        String nombreVar = this.getNombreP();
        if (!nombreVar.startsWith("%")) {
            nombreVar = "%" + nombreVar;
        }

        // SI EL DESTINO ES UN ARREGLO (Maneja tanto FLOAT_ARRAY como la dimensión en dígitos del nuevo validador)
        if (tipoNormalizado.contains("ARRAY") || tipoNormalizado.matches("\\d+") || this.tipo.matches("\\d+")) {

            // 🌟 CORREGIDO: Si el tipo consolidado es un número dígito, esa es la dimensión.
            // Si no, la dimensión debería venir en el propio 'idTipo' o 'this.tipo'.
            String tamano = this.tipo.matches("\\d+") ? this.tipo : tipoNormalizado;
            if (!tamano.matches("\\d+")) {
                tamano = "10"; // Respaldo seguro por defecto si no se logra extraer numéricamente
            }

            String tipoEstructuraLLVM = "[" + tamano + " x double]";

            // 1. Obtener el puntero a la celda 0 del arreglo
            String ptrDestinoReal = CodeGeneratorHelper.getNewPointer();
            resultado.append(String.format("  %1$s = getelementptr %2$s, ptr %3$s, i64 0, i64 0\n",
                    ptrDestinoReal, tipoEstructuraLLVM, nombreVar));

            // 2. Delegación de código para operaciones vectoriales
            if (this.valor instanceof ast.OperacionBinaria) {
                resultado.append(((ast.OperacionBinaria) this.valor).generarCodigoConDestino(ptrDestinoReal));
            } else {
                if (this.valor != null) resultado.append(this.valor.generarCodigo());
            }
            return resultado.toString();
        }

        // CAMINO ESCALAR TRADICIONAL
        if (this.valor != null) {
            resultado.append(this.valor.generarCodigo());
        }

        switch (tipoNormalizado) {
            case "INT" ->
                    resultado.append(String.format("  store i32 %1$s, ptr %2$s\n", this.valor.getIr_ref(), nombreVar));
            case "FLOAT" -> // 🌟 Agregado DOUBLE por si acaso tu backend lo usa
                    resultado.append(String.format("  store double %1$s, ptr %2$s\n", this.valor.getIr_ref(), nombreVar));
            case "BOOLEAN" ->
                    resultado.append(String.format("  store i1 %1$s, ptr %2$s\n", this.valor.getIr_ref(), nombreVar));
        }

        return resultado.toString();
    }

    @Override
    public String getTipo() {
        return tipo;
    }
}