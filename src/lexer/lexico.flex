/****************************************************************************
 * En esta sección se puede incluir todo código que se copiará textualmente
 * al comienzo del archivo JAVA que contendrá la definición de la clase del 
 * analizador léxico.
 ****************************************************************************/
package lexer;
import java.util.ArrayList;

/****************************************************************************
 * Las siguientes directivas afectan el comportamiento del analizador léxico:
 *
 *  - %class Nombre --> Nombre de la clase generada.
 *  - %type Nombre  --> Tipo retornado por yylex().
 *  - %line         --> Número de línea (this.yyline)
 *  - %column       --> Número de columna (this.yycolumn)
 *
 * Existen otras directivas adicionales descriptas en la documentación.
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



/* ── Espacios y terminadores ────────────────────────────────────────────── */
LineTerminator = \r\n | \r | \n
HSpace         = [ \t]

/* ── Línea en blanco (sólo espacios + fin de línea) ────────────────────── */
BlankLine      = {HSpace}* {LineTerminator}

/* ── Variables y constantes ────────────────────────────────────────────── */

IDENTIFICADOR  = [a-zA-Z][a-zA-Z0-9_]*
ENTERO         = [0-9]+
FLOAT          = ([0-9]+\.[0-9]+)|([0-9]+\.)|(\.[0-9]+)
BOOLEAN        = true|false
FLOAT_ARRAY=\[\s*-?{FLOAT}(\s*,\s*-?{FLOAT})*\s*\]

COMENTARIO_SIMPLE_LLAVES = \{.*\}
COMENTARIO_SIMPLE = %.*\n
COMENTARIO_MULTILINEA_PARENTESIS = \(\*([^*]|\*+[^)])*\*+\)
COMENTARIO_MULTILINEA_CORCHETE = \[\*([^*]|\*+[^\]])*\*+\]
COMENTARIO_MULTILINEA_LLAVES = \{\*([^*]|\*+[^\}])*\*+\}


/* ── Estados adicionales ────────────────────────────────────────────────── */
%state LINE_START


%%

/* ════════════════════════════════════════════════════════════════════════
   LINE_START
   Mide la indentación al comienzo de una línea nueva.
   Transiciona a YYINITIAL cuando encuentra el primer carácter real.
   ════════════════════════════════════════════════════════════════════════ */
<LINE_START> {

    /* Línea completamente en blanco → ignorar  */
    {BlankLine}  {}

    /* Espacio simple: un nivel                                            */
    " "   { pendingIndent++; }

    /* Tabulación: suma 4                    */
    "\t"  { pendingIndent +=  4; }

    /* Primer carácter real: procesar cambio de indentación               */
    [^ \t\r\n] {
                   yypushback(1);              // devolver el carácter para que sea leído por el YYINITIAL
                   yybegin(YYINITIAL);
                   processIndent(pendingIndent, yyline, yycolumn);
                   pendingIndent = 0;
                   if (!pendingTokens.isEmpty()) {
                       return pendingTokens.poll();
                   }
               }
}


/* ════════════════════════════════════════════════════════════════════════
   YYINITIAL — análisis normal dentro de una línea
   ════════════════════════════════════════════════════════════════════════ */
<YYINITIAL> {

    /* ===== OPERADORES ARITMÉTICOS ===== */

    "+"        { return token("OP_SUMA", yytext()); }
    "-"        { return token("OP_RESTA", yytext()); }
    "*"        { return token("OP_MULTI", yytext()); }
    "/"        { return token("OP_DIV",  yytext()); }

    /* ===== OTROS OPERADORES ===== */

    "="        { return token("OP_ASIGNACION", yytext()); }
    "("        { return token("PARENTESIS_IZQ", yytext()); }
    ")"        { return token("PARENTESIS_DER", yytext()); }

    /* ===== OPERADORES LÓGICOS ===== */

    "||"       { return token("OP_OR", yytext()); }
    "&&"       { return token("OP_AND", yytext()); }
    "!"        { return token("OP_NOT", yytext()); }

    /* ===== OPERADORES DE COMPARACIÓN ===== */

    "=="       { return token("OP_IGUAL", yytext()); }
    "!="       { return token("OP_DESIGUAL", yytext()); }
    "<"        { return token("OP_MENOR", yytext()); }
    "<="       { return token("OP_MENORIGUAL", yytext()); }
    ">"        { return token("OP_MAYOR", yytext()); }
    ">="       { return token("OP_MAYORIGUAL", yytext()); }

    /* ===== ITERACIÓN ===== */

    "while"     { return token("WHILE", yytext()); }
    "alt_while" { return token("ALT_WHILE", yytext()); }
    "break"     { return token("BREAK", yytext()); }
    "continue"  { return token("CONTINUE", yytext()); }

    /* ===== SELECCIÓN ===== */

    "if"        { return token("IF", yytext()); }
    "else"      { return token("ELSE", yytext()); }
    "elif"      { return token("ELIF", yytext()); }

    /* ===== TIPOS ===== */

    "boolean"        { return token("TIPO_BOOL", yytext()); }
    "integer"        { return token("TIPO_INT", yytext()); }
    "float"          { return token("TIPO_FLOAT", yytext()); }
    "float_array"    { return token("TIPO_ARRAY", yytext()); }

    /* ===== ENTRADA-SALIDA ===== */

    "print"         { return token("PRINT", yytext()); }
    "read_int"      { return token("READ_INT", yytext()); }
    "read_float"    { return token("READ_FLOAT", yytext()); }
    "read_bool"     { return token("READ_BOOL", yytext()); }

    /* ===== OTROS ===== */

    "PROGRAM"   { return token("PROGRAM", yytext()); }
    ","         { return token("COMA", yytext()); }
    "."         { return token("PUNTO", yytext()); }
    "FIN"       { return token("FIN", yytext()); }

    /* ===== CONSTANTES ===== */

    {FLOAT}       { return token("FLOAT", yytext()); }
    {ENTERO}      { return token("ENTERO", yytext()); }
    {FLOAT_ARRAY} { return token("FLOAT_ARRAY", yytext()); }
    {BOOLEAN}     { return token("BOOLEAN", yytext()); }

    /* ===== VARIABLES ===== */

    {IDENTIFICADOR}        { return token("ID", yytext()); }

    /*========COMENTARIOS==========*/
    {COMENTARIO_MULTILINEA_PARENTESIS}    { return token("COMENTARIO_MULTILINEA_PARENTESIS", yytext()); }
    {COMENTARIO_MULTILINEA_CORCHETE}      { return token("COMENTARIO_MULTILINEA_CORCHETE", yytext()); }
    {COMENTARIO_MULTILINEA_LLAVES}        { return token("COMENTARIO_MULTILINEA_LLAVES", yytext()); }
    {COMENTARIO_SIMPLE_LLAVES}            { return token("COMENTARIO_SIMPLE_LLAVES", yytext()); }
    {COMENTARIO_SIMPLE}                   { return token("COMENTARIO_SIMPLE", yytext()); }


    {LineTerminator}   {
                         yybegin(LINE_START);
                       }
    {HSpace}+ { /* ignorar */ }

}


/* ── Carácter ilegal (fallback) ──────────────────────────────────────── */
[^] { return token("ERROR", "Error: carácter ilegal <" + yytext() + ">"); }