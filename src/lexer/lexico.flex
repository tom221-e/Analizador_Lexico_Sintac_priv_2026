package lexer;
import java.util.ArrayList;

/****************************************************************************
 * Analizador léxico con manejo de estados para Strings y Comentarios.
 * Proyecto UNNOBA 2026. [cite: 1, 2]
 ****************************************************************************/

%%

%public
%class Lexer
%implements java_cup.runtime.Scanner
%unicode
%type Token
%line
%column

%{
    private StringBuilder stringBuffer = new StringBuilder();
    /* ─────────────────────────────────────────────────────────────────────
     * Variables de instancia
     * ───────────────────────────────────────────────────────────────────── */

    // ── Indentación significativa ────────────────────────────────────────
    /** Pila de niveles de indentación. El fondo siempre es 0. */
    java.util.Deque<Integer> indentStack = new java.util.ArrayDeque<>();

    /**
     * Cola de tokens generados pero aún no entregados al parser.
     * Se usa cuando una sola acción léxica produce más de un token
     */
    java.util.Queue<Token> pendingTokens = new java.util.LinkedList<>();

    /** Nivel de indentación acumulado mientras se escanea LINE_START. */
    int pendingIndent = 0;

    // Bloque de inicialización de instancia
    {
        indentStack.push(0);    // nivel base: columna 0
    }

    /* ─────────────────────────────────────────────────────────────────────
     * Métodos auxiliares para construir tokens
     * ───────────────────────────────────────────────────────────────────── */
    private Token token(String nombre) {
        return new Token(nombre, yyline, yycolumn);
    }

    private Token token(String nombre, Object valor) {
        return new Token(nombre, yyline, yycolumn, valor);
    }

    private Token token(String nombre, int line, int col, Object valor) {
        return new Token(nombre, line, col, valor);
    }

    /* ─────────────────────────────────────────────────────────────────────
     * processIndent(indent, line, col)
     *
     * Compara `indent` con el tope de la pila y produce los tokens
     * necesarios.  El primero se retorna; los adicionales van a
     * pendingTokens para ser entregados en llamadas posteriores.
     *
     *   indent > top  →  devuelve INDENT  (apila nuevo nivel)
     *   indent < top  →  devuelve DEDENT y encola N x DEDENT
     * ───────────────────────────────────────────────────────────────────── */
    private void processIndent(int indent, int line, int col) {
        int top = indentStack.peek();

        if (indent > top) {
            indentStack.push(indent);
            pendingTokens.add(new Token("INDENT", line, col));

        } else if (indent < top) {
            while (indentStack.peek() > indent) {
                indentStack.pop();
                pendingTokens.add(new Token("DEDENT", line, col));
            }

            // Verificar consistencia del nivel
            if (indentStack.peek() != indent) {
                throw new RuntimeException("Error: indentación inconsistente en línea " + line);
            }
        }
    }


    /* ─────────────────────────────────────────────────────────────────────
     * next_token() — requerido por java_cup.runtime.Scanner.
     * Sobrescribe el método generado por JFlex para manejar la cola de tokens
     *
     * Flujo:
     *   1. Si hay tokens pendientes, los entrega de a uno.
     *   2. Si no, llama a yylex().
     *   3. Si yylex() retorna null (EOF), cierra bloques con DEDENT y
     *      entrega el primero.
     *   4. Cuando la cola se vacía tras el EOF, retorna null: scan() de
     *      CUP lo convierte en END_OF_FILE.
     * ───────────────────────────────────────────────────────────────────── */
    public Token next_token() throws java.io.IOException {
        if (!pendingTokens.isEmpty()) {
            return pendingTokens.poll();
        }
        Token t = yylex();
        if (t == null) {
            processIndent(0, yyline, yycolumn);
            t = pendingTokens.isEmpty() ? null : pendingTokens.poll();
        }
        return t;
    }
%}



/* --- Definiciones --- */
LineTerminator = \r\n | \r | \n
HSpace         = [ \t]
ID             = [\p{L}][\p{L}0-9_]*
ENTERO         = [0-9]+
FLOAT          = ([0-9]+\.[0-9]+)|([0-9]+\.)|(\.[0-9]+)
BOOLEAN        = true|false
FLOAT_ARRAY    = \[\s*-?{FLOAT}(\s*,\s*-?{FLOAT})*\s*\]

/* --- Estados --- */
%state COMMENT_LINE
%state LINE_START
%state STRING_STATE
%state COMMENT_PAREN
%state COMMENT_BRACKET
%state COMMENT_BRACE


%%

/* ════════════════════════════════════════════════════════════════════════
   LINE_START: Medición de indentación
   ════════════════════════════════════════════════════════════════════════ */
<LINE_START> {
    {LineTerminator} { /* Línea vacía: ignorar */ }
    " "              { pendingIndent++; }
    "\t"             { pendingIndent += 4; }

    /* Inicio de comentario en inicio de línea: ignorar indentación y saltar */
    "(*"             { yybegin(COMMENT_PAREN); }
    "[*"             { yybegin(COMMENT_BRACKET); }
    "{*"             { yybegin(COMMENT_BRACE); }

    [^ \t\r\n]       {
                       yypushback(1);
                       yybegin(YYINITIAL);
                       processIndent(pendingIndent, yyline, yycolumn);
                       pendingIndent = 0;
                       if (!pendingTokens.isEmpty()) return pendingTokens.poll();
                     }
}

/* ════════════════════════════════════════════════════════════════════════
   YYINITIAL: Análisis normal
   ════════════════════════════════════════════════════════════════════════ */
<YYINITIAL> {

    /* --- Manejo de Strings con estado --- */

    \" {
          stringBuffer = new StringBuilder();
          yybegin(STRING_STATE);
    }


    /* --- Comentarios (No retornan token) --- */
    "(*"             { yybegin(COMMENT_PAREN); }
    "[*"             { yybegin(COMMENT_BRACKET); }
    "{*"             { yybegin(COMMENT_BRACE); }
"%" { yybegin(COMMENT_LINE); }
    /*        "%"[^%\r\n]*"%"  { /* ignorar comentario de una línea */ }        */


    /* --- Palabras Reservadas --- */
    "PROGRAM"        { return token("PROGRAM", yytext()); }
    "if"             { return token("IF", yytext()); }
    "else"           { return token("ELSE", yytext()); }
    "while"          { return token("WHILE", yytext());}
    "alt_while"      { return token("ALT_WHILE", yytext()); }
    "elif"           { return token("ELIF", yytext()); }
    "begin"          { return token("BEGIN", yytext());}
    "end"            { return token("END", yytext()); }
    "print"          { return token("PRINT", yytext()); }
    "break"          { return token("BREAK", yytext()); }
    "continue"       { return token("CONTINUE", yytext()); }
    "INT"            { return token("TYPE_INT", yytext()); }
    "FLOAT"          { return token("TYPE_FLOAT", yytext()); }
    "BOOLEAN"        { return token("TYPE_BOOL", yytext()); }
    "ARRAY"          { return token("TYPE_ARRAY", yytext()); }
    "valor_mas_cercano" { return token("VALOR_MAS_CERCANO", yytext()); }

    /* --- Operadores y Símbolos --- */
    "+"              { return token("OP_SUMA", yytext()); }
    "-"              { return token("OP_RESTA", yytext()); }
    "*"              { return token("OP_MULTI", yytext()); }
    "/"              { return token("OP_DIV", yytext()); }
    "="              { return token("IGUAL", yytext()); }
    "||"             { return token("OR", yytext()); }
    "&&"             { return token("AND", yytext()); }
    "!"              { return token("NOT", yytext()); }
    "=="             { return token("IGUALDAD", yytext()); }
    "<="             { return token("MENORIGUAL", yytext()); }
    ">="             { return token("MAYORIGUAL", yytext()); }
    "<"              { return token("MENOR", yytext()); }
    ">"              { return token("MAYOR", yytext()); }
    "("              { return token("PAREN_A", yytext()); }
    ")"              { return token("PAREN_C", yytext()); }
    "["              { return token("CORCH_A", yytext()); }
    "]"              { return token("CORCH_C", yytext()); }
    ":"              { return token("DOS_PUNTOS", yytext()); }
    ","              { return token("COMA", yytext()); }

    /* --- Tipos de Datos --- */
    {BOOLEAN}        { return token("BOOLEAN", yytext()); }
    {FLOAT}          { return token("FLOAT", yytext()); }
    {ENTERO}         { return token("ENTERO", yytext()); }
    {FLOAT_ARRAY}    { return token("FLOAT_ARRAY", yytext()); }
    {ID}             { return token("ID", yytext()); }

    /* --- Funciones de Lectura --- */
    "READ_INT()"     { return token("READ_INT", yytext()); }
    "READ_FLOAT()"   { return token("READ_FLOAT", yytext()); }
    "READ_BOOL()"    { return token("READ_BOOL", yytext()); }

    {LineTerminator} { yybegin(LINE_START); }
    {HSpace}+        { /* ignorar */ }
}

/* ════════════════════════════════════════════════════════════════════════
   ESTADOS DE STRING Y COMENTARIOS
   ════════════════════════════════════════════════════════════════════════ */

<STRING_STATE> {
    "\""             { yybegin(YYINITIAL); return token("STRING", stringBuffer.toString()); }
    "\\n"            { stringBuffer.append('\n'); }
    "\\t"            { stringBuffer.append('\t'); }
    "\\\""           { stringBuffer.append('\"'); }
    "\\\\"           { stringBuffer.append('\\'); }
    {LineTerminator} { throw new RuntimeException("Error: String literal no cerrado en línea " + (yyline+1)); }
    [^\"\\\r\n]+     { stringBuffer.append(yytext()); }
    <<EOF>>          { throw new RuntimeException("Error: Fin de archivo inesperado dentro de String"); }
}

<COMMENT_PAREN> {
    "*)"             { yybegin(YYINITIAL); }
    [^]              { /* ignorar */ }
    <<EOF>>          { throw new RuntimeException("Error: Comentario (* no cerrado"); }
}

<COMMENT_BRACKET> {
    "*]"             { yybegin(YYINITIAL); }
    [^]              { /* ignorar */ }
    <<EOF>>          { throw new RuntimeException("Error: Comentario [* no cerrado"); }
}

<COMMENT_BRACE> {
    "*}"             { yybegin(YYINITIAL); }
    [^]              { /* ignorar */ }
    <<EOF>>          { throw new RuntimeException("Error: Comentario {* no cerrado"); }
}

<COMMENT_LINE> {
    "%" {
        yybegin(YYINITIAL);   // cierre correcto
    }

    \r|\n {
        throw new RuntimeException("Error: comentario de línea no cerrado con %");
    }

    [^%\r\n]+ {
        /* ignorar contenido */
    }

    <<EOF>> {
        throw new RuntimeException("Error: comentario no cerrado (EOF)");
    }
}

/* --- Fallback Error --- */
[^] { return token("ERROR", "Error: carácter ilegal <" + yytext() + ">"); }