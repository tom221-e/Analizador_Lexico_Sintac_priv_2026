package ast;
import java.util.ArrayList;

public class SentenciaElse extends Bloque {
    public SentenciaElse(ArrayList<Sentencia> sentencias) {
        super(sentencias);
    }

    @Override
    protected String getNombreSentencia() {
        return "ELSE"; // Esto es lo que aparecerá en el "globo" padre
    }

    @Override
    public String generarCodigo() {
        // Delegamos la responsabilidad a la clase padre Bloque.
        // Bloque se encargará de iterar el ArrayList y concatenar
        // el generarCodigo() de cada sentencia interna de forma lineal.
        return super.generarCodigo();
    }
}