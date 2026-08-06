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
        String nombreVar = getNombreP(); // Esta es la dirección de la variable real (ej: %a1)
        String tipoNormalizado = idTipo != null ? idTipo.toUpperCase() : "";



        // 1. Generar el valor (la operación o el literal)
        if (this.valor != null) {
            resultado.append(this.valor.generarCodigo());
        }

        // 2. Si es una operación de ARREGLO
        if (tipoNormalizado.contains("ARRAY") || (this.valor instanceof OperacionBinaria && this.valor.getTipo().matches("\\d+"))) {

            String ptrOrigen = this.valor.getIr_ref(); // Este es el temporal creado por OperacionBinaria

            // Si el puntero de origen es distinto al destino, necesitamos copiarlo
            if (ptrOrigen != null && !ptrOrigen.equals(nombreVar)) {
                // Obtenemos el tamaño del arreglo (o el valor de 'tipo')
                String tamano = this.valor.getTipo().matches("\\d+") ? this.valor.getTipo() : "10";
                long bytes = Long.parseLong(tamano) * 8; // double = 8 bytes

                resultado.append(String.format("  call void @llvm.memcpy.p0.p0.i64(ptr %1$5s, ptr %2$s, i64 %3$s, i1 false)\n",
                        nombreVar, ptrOrigen, bytes));
            }
            }

        // --- CAMINO ESCALAR TRADICIONAL ---
        String valorRef = this.valor.getIr_ref();

        switch (tipoNormalizado) {
            case "INT" -> resultado.append(String.format("  store i32 %1$s, ptr %2$s\n", valorRef, nombreVar));
            case "FLOAT" -> resultado.append(String.format("  store double %1$s, ptr %2$s\n", valorRef, nombreVar));
            case "BOOLEAN" -> resultado.append(String.format("  store i1 %1$s, ptr %2$s\n", valorRef, nombreVar));
        }

            return resultado.toString();
        }
}