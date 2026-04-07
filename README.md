# Compilador - Analizador Léxico + Sintáctico (UNNOBA 2026)

Analizador léxico y sintáctico de ejemplo construido con **JFlex** y **java-cup**.

## Estructura del proyecto

```
Compilador_ejemplos/
├── pom.xml
└── src/
    ├── Generador.java              ← Regenera Lexer.java + Parser.java desde las fuentes
    ├── input_1.txt                 ← Archivo de prueba
    ├── lexer/
    │   ├── lexico.flex             ← Definición del léxico (fuente JFlex)
    │   ├── Lexer.java              ← Léxico generado por JFlex  ⟩ generados,
    │   ├── Token.java              ← Clase token                ⟩ se pueden
    │   └── Main_lexer.java         ← Punto de entrada: solo análisis léxico
    └── parser/
        ├── parser.cup              ← Definición de la gramática (fuente java-cup)
        ├── ParserSym.java          ← Constantes de terminales   ⟩ regenerar
        ├── Parser.java             ← Parser generado por CUP    ⟩ ejecutando
        └── Main_parser.java        ← Punto de entrada: análisis léxico + sintáctico
```

> **Nota:** `Lexer.java`, `Parser.java` y `ParserSym.java` son archivos
> **generados**. Si se modifican `lexico.flex` o `parser.cup`, deben regenerarse
> ejecutando `Generador.java`.

## Prerrequisitos

- Java 21 o superior
- Maven 3.6 o superior

## Primer uso: regenerar el léxico y el parser

Si se modificaron `lexico.flex` o `parser.cup`:

```bash
mvn exec:java -Dexec.mainClass="Generador"
```

Esto produce `Lexer.java`, `Parser.java` y `ParserSym.java`.

## Compilar

```bash
mvn clean compile
```

## Ejecutar el analizador léxico

```bash
mvn exec:java -Dexec.mainClass="lexer.Main_lexer"
```

Al ejecutar, el programa pregunta:

```
=== Analizador Léxico ===
¿Desde dónde desea leer?
  1 - Desde consola
  2 - Desde archivo (./src/input_1.txt)
Ingrese su opción:
```

- **Opción 1 (consola):** escribí tokens línea a línea. Ingresá `FIN` para terminar.
- **Opción 2 (archivo):** lee y muestra todos los tokens de `input_1.txt`.

## Ejecutar el analizador sintáctico

```bash
mvn exec:java -Dexec.mainClass="parser.Main_parser"
```

Lee directamente desde `./src/input_1.txt` y aplica las reglas de `parser.cup`
sobre la secuencia de tokens, imprimiendo cada reducción aplicada.

## Diseño del léxico (`lexico.flex`)

El léxico usa `%implements java_cup.runtime.Scanner` y `%type Token`.
El método `next_token()` —requerido por la interfaz `Scanner`— simplemente
delega en `yylex()`, sin lógica adicional:

```java
public java_cup.runtime.Symbol next_token() throws java.io.IOException {
    return yylex();
}
```

## Clase Token

`Token` extiende `ComplexSymbol` (de java-cup) y resuelve su número de terminal
buscando el nombre en el array `ParserSym.terminalNames`. Almacena nombre, línea,
columna y valor léxico.

## Token FIN

`FIN` es una palabra reservada del léxico. En `Main_lexer`, al reconocerlo,
se corta el análisis. En `Main_parser` no se usa `FIN`; el análisis termina
naturalmente cuando el lexer retorna `null` al llegar al fin del archivo.

## Tokens reconocidos

| Token           | Descripción                      |
|-----------------|----------------------------------|
| `FIN`           | Corta la ejecución (solo léxico) |
| `MAS`           | `+`                              |
| `MENOS`         | `-`                              |
| `MULT`          | `*`                              |
| `DIV`           | `/`                              |
| `PAR_ABRE`      | `(`                              |
| `PAR_CIERRA`    | `)`                              |
| `IDENTIFICADOR` | Variable (`abc`, `_x1`)          |
| `ENTERO`        | Constante entera (`42`)          |

## Gramática (`parser.cup`)

La gramática reconoce expresiones aritméticas con las siguientes reglas
y precedencias (de menor a mayor):

```
expression ::= expr
expr ::= expr MAS expr
       | expr MENOS expr
       | expr MULT expr
       | expr DIV expr
       | PAR_ABRE expr PAR_CIERRA
       | IDENTIFICADOR
       | ENTERO
       | MENOS expr          (%prec MENOS_UNARIO)
```

Las precedencias están declaradas como `left` para `MAS`/`MENOS`, luego
`MULT`/`DIV`, y finalmente `MENOS_UNARIO` para el menos unario.
