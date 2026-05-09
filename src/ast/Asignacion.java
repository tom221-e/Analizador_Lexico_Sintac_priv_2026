package ast;

public class Asignacion extends Sentencia {
    private final String id;
    private final Expresion valor;

    public Asignacion(String id, Expresion valor) {
        this.id = id;
        this.valor = valor;
    }

    @Override
    protected String getNombreSentencia() {
        return "ASIGNACION: " + id; // El globo del grafo mostrará el nombre de la variable
    }

    @Override
    protected String graficar(String idPadre) {
        String miId = this.getId();
        // Grafica el nodo de asignación y cuelga de él la expresión del valor
        return super.graficar(idPadre) +
                valor.graficar(miId);
    }
}