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
}