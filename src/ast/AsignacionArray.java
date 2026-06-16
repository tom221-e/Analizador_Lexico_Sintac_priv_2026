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

        // 🌟 REFACTORIZACIÓN: Formatear el globo izquierdo como NOMBRE[INDICE]
        // Si el índice es un IdLiteral o IntLiteral, intentamos extraer su identificador/valor
        String representacionIndice = "?";
        if (this.indice instanceof ast.literal.IdLiteral) {
            representacionIndice = ((ast.literal.IdLiteral) this.indice).getNombreVariable();
        } else if (this.indice instanceof ast.literal.IntLiteral) {
            // Asumiendo que tienes un método similar o puedes usar getEtiqueta/toString
            representacionIndice = this.indice.getTipo(); // O el atributo que guarde el número
            if (representacionIndice == null || "INT".equals(representacionIndice)) {
                representacionIndice = indice.getValor(); // Respaldo genérico si es una variable indexada compleja
            }
        } else {
            representacionIndice = "expr"; // Si es una expresión matemática compleja: ej: mediciones[i + 1]
        }

        // Construimos el string exacto para el globo: ej: "mediciones[i]"
        String nombreGloboIzquierdo = this.nombre + "[" + representacionIndice + "]";

        // 2. Graficamos el lado IZQUIERDO (Pasamos el nombre modificado NOMBRE[INDICE])
        IdLiteral nodoId = new IdLiteral(nombreGloboIzquierdo, "FLOAT_ARRAY");
        grafico.append(nodoId.graficar(miId));

        // 3. Graficamos el lado DERECHO (el valor o expresión)
        grafico.append(valor.graficar(miId));

        return grafico.toString();
    }

    // Método auxiliar para obtener el puntero formateado de LLVM para esta variable (%nombre)
    protected String getNombreP() {
        return "%" + nombre;
    }

    /*
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
    */
    
    @Override
    public String generarCodigo() {
        StringBuilder resultado = new StringBuilder();

        // 1. generar código del índice y del valor
        resultado.append(this.indice.generarCodigo());
        resultado.append(this.valor.generarCodigo());

        // 2. extender índice a i64
        String ptrIndice64 = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %1$s = sext i32 %2$s to i64\n",
                ptrIndice64,
                this.indice.getIr_ref()
        ));

        // === CHEQUEO DE BOUNDS ===

        // verificar índice >= 0
        String checkNeg = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %s = icmp sge i32 %s, 0\n",
                checkNeg, this.indice.getIr_ref()));

        // verificar índice < tamaño
        String checkMax = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %s = icmp slt i32 %s, %s\n",
                checkMax, this.indice.getIr_ref(), this.tamano));

        // ambas condiciones deben ser true
        String checkOk = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format("  %s = and i1 %s, %s\n",
                checkOk, checkNeg, checkMax));

        // bifurcar según resultado
        String labelOk    = CodeGeneratorHelper.getNewTag();
        String labelError = CodeGeneratorHelper.getNewTag();
        resultado.append(String.format("  br i1 %s, label %%%s, label %%%s\n\n",
                checkOk, labelOk, labelError));

        // bloque error
        resultado.append(labelError).append(":\n");
        String msgReg  = CodeGeneratorHelper.getNewPointer();
        String callReg = CodeGeneratorHelper.getNewPointer();
        resultado.append(String.format(
                "  %s = call i32 (i8*, ...) @printf(ptr @.bounds_error)\n", msgReg));
        resultado.append(String.format(
                "  %s = call i32 @exit(i32 1)\n", callReg));
        resultado.append("  unreachable\n\n");

        // bloque acceso válido
        resultado.append(labelOk).append(":\n");

        // === FIN CHEQUEO DE BOUNDS ===

        // 3. calcular dirección del elemento
        String ptrElemento = CodeGeneratorHelper.getNewPointer();
        String tipoEstructura = "[" + this.tamano + " x double]";
        resultado.append(String.format("  %1$s = getelementptr inbounds %2$s, ptr %3$s, i64 0, i64 %4$s\n",
                ptrElemento,
                tipoEstructura,
                this.getNombreP(),
                ptrIndice64
        ));

        // 4. almacenar el valor
        resultado.append(String.format("  store double %1$s, ptr %2$s\n",
                this.valor.getIr_ref(),
                ptrElemento
        ));

        return resultado.toString();
    }
    
}