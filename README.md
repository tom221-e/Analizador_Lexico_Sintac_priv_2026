# Compilador - Analizador Léxico + Sintáctico (UNNOBA 2026)

Analizador léxico y sintáctico construido con **JFlex** y **java-cup**.

Compiladores 2026 - Grupo 3
* Tomas Esparza
* MARINA LILIAN RODRIGUEZ
* FANNY BELÉN VIZCAÍNO CORSETTI

## Estructura del proyecto

```
Compilador_ejemplos/
├── pom.xml
│── src/
├── Funcion/
│   └── array_alu.ll          ← Librería externa en LLVM para operaciones vectoriales
├── src/
│    ├── ast/                  ← 🌟 NUEVO: Nodos del Árbol de Sintaxis Abstracta y Generación IR
│    ├── lexer/
│    │   ├── lexico.flex             ← Definición del léxico (fuente JFlex)
│    │   ├── Lexer.java              ← Léxico generado por JFlex
│    │   ├── Token.java              ← Clase token
│    │   └── Main_lexer.java         ← Punto de entrada: solo análisis léxico
│    ├── parser/
│    │    ├── parser.cup             ← Definición de la gramática (fuente java-cup)
│    │    ├── ParserSym.java         ← Constantes de terminales
│    │    ├── Parser.java            ← Parser generado por CUP 
│    │    ├── Main_parser.java       ← Punto de entrada: análisis léxico + sintáctico
│    │    └── SymbolTable.java       ← Tabla de símbolos
│    ├── Generador.java              ← Regenera Lexer.java + Parser.java desde las fuentes
│    ├── input_files/                ← Archivos de prueba
│    ├──llvm/
│        └── CodeGeneratorHelper.java ← Funciones auxiliares para la generacion de codigo LLVM
│    ├──validator/
│        └── ValidatorDataType.java  ← Funciones que validan los tipos de operaciones
└── target/                			 ← Archivo compilado (.jar) con interfaz grafica
     ├── Funcion/
            └── array_alu.ll          ← Librería externa en LLVM para operaciones vectoriales

```

> **Nota:** `Lexer.java`, `Parser.java` y `ParserSym.java` son archivos
> **generados**. Si se modifican `lexico.flex` o `parser.cup`, deben regenerarse
> ejecutando `Generador.java`.

## Prerrequisitos

- Java 21 o superior
- Maven 3.6 o superior
- JFlex
- CUP
  
*NUEVO
- LLVM & Visual Basic Toola
- Graphviz

## Primer uso: regenerar el léxico y el parser

Si se modificaron `lexico.flex` o `parser.cup`:

```bash
mvn exec:java -Dexec.mainClass="Generador"
```

Esto produce `Lexer.java`, `Parser.java` y `ParserSym.java`.

## Compilar

```bash
mvn clean package
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
  2 - Desde archivo
Ingrese su opción:
```

- **Opción 1 (consola):** escribí tokens línea a línea. Ingresá `FIN` para terminar.
- **Opción 2 (archivo):** muestra todos los archivos de `input_files` y lee el seleccionado.

```
Seleccione un archivo:
  1 - input_1.txt
  2 - input_2.txt
  ...
Ingrese el número del archivo: 
```

## Ejecutar el analizador sintáctico

```bash
mvn exec:java -Dexec.mainClass="parser.Main_parser"
```

Lee el archivo seleccionado y aplica las reglas de `parser.cup`
sobre la secuencia de tokens, imprimiendo cada reducción aplicada.

```
=== Analizador Sintáctico ===
Seleccione un archivo:
  1 - input_1.txt
  2 - input_2.txt
  ...
Ingrese el número del archivo: 
```
Desde el analizador sintáctico se puede crear el AST, el .ll y el .exe

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

## Archivo Compilador

El archivo ya compilado en formato .jar se puede encontrar dentro de la 
carpeta "target" ya incluye las dependecias basicas pero sigue nesesitando
el java 21 o superior para funcionar. Es muy intuitivo, el funcionamiento
es directo y sigue un flujo lineal de tres pasos: Carga, Configuración y 
Ejecución.

* Preparación del Código: El usuario puede escribir directamente su código
en el panel de Entrada o utilizar el botón Cargar Archivo para importar
un documento de texto existente.

* Selección del Análisis: Mediante los botones de opción, el usuario decide
qué fase del compilador desea poner a prueba:
	* Modo Léxico: Para verificar que las palabras y símbolos sean reconocidos
	correctamente (Tokens).
	* Modo Sintáctico: Para validar que la estructura y el orden de las
	sentencias cumplan con las reglas de la gramática.

* Procesamiento y Resultados: Al presionar Ejecutar, el sistema procesa el
texto y muestra instantáneamente en la Salida el resultado detallado (lista
de tokens o tabla de símbolos). Si existe algún error en el código ingresado,
el usuario lo verá resaltado en ese mismo panel de salida.

* Exportación: Finalmente, si el resultado es satisfactorio o se necesita reporta
 un error, el usuario utiliza el botón Guardar Salida para generar un reporte
 en un archivo `.txt`.

**NUEVO

	Se agregaron las Opciones al .jar para generar el arbol AST de parsing
	y para compilar a .ll y .exe el programa, para lo cual se nesesitan las
	nuevas herramientas
