package lexer;


import java_cup.runtime.ComplexSymbolFactory.ComplexSymbol;
import parser.ParserSym;

import java.util.Arrays;

/**
 * Representa un token producido por el analizador léxico.
 * Extiende ComplexSymbol para ser compatible con java-cup.
 */
public class Token extends ComplexSymbol {

    public final String nombre;
    public final int linea;
    public final int columna;
    public final Object valor;

    public Token(String nombre, int linea, int columna) {
        this(nombre, linea, columna, null);
    }

    public Token(String nombre, int linea, int columna, Object valor) {
        super(nombre, Arrays.asList(ParserSym.terminalNames).indexOf(nombre), valor);

        this.nombre  = nombre;
        this.linea   = linea;
        this.columna = columna;
        this.valor   = valor;
    }

    @Override
    public String toString() {
        String pos = " @ (L:" + linea + ", C:" + columna + ")";
        if (valor == null)
            return "[" + nombre + "]" + pos;
        else
            return "[" + nombre + "] -> (" + valor + ")" + pos;
    }
}
