package gui;

import ast.Programa;
import java.io.*;
import java.nio.file.Files;
import javax.swing.*;
import java.awt.*;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;

public class VentanaPrueba extends javax.swing.JFrame {

    private JTextPane input;
    private JTextArea txtOutput;
    private JRadioButton sLexico;
    private JRadioButton sSintactico;
    private JButton run;
    private JButton btnCargar;
    private JButton btnGuardar;
    private JButton btnGenerarAST;
    private JButton btnGenerarLLVM;
    private JScrollPane scrollInput;
    private JScrollPane scrollOutput;
    private ButtonGroup grupoOpciones;

    public VentanaPrueba() {
        super("Analizador de Compiladores");
        initComponents();
        configurarVentana();
    }

    private void configurarVentana() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(950, 550);
        this.setLocationRelativeTo(null);

        grupoOpciones = new ButtonGroup();
        grupoOpciones.add(sLexico);
        grupoOpciones.add(sSintactico);
        sSintactico.setSelected(true);
    }

    private void initComponents() {
        input = new JTextPane();
        scrollInput = new JScrollPane(input);

        txtOutput = new JTextArea();
        txtOutput.setEditable(false);
        txtOutput.setBackground(new Color(240, 240, 240));
        scrollOutput = new JScrollPane(txtOutput);

        sLexico = new JRadioButton("Léxico");
        sSintactico = new JRadioButton("Sintáctico");
        run = new JButton("Ejecutar");

        btnCargar = new JButton("Cargar Archivo");
        btnGuardar = new JButton("Guardar Salida");
        btnGenerarAST = new JButton("Generar AST");
        btnGenerarLLVM = new JButton("Generar .ll y .exe");

        JLabel lblEntrada = new JLabel("Entrada:");
        JLabel lblSalida = new JLabel("Salida (Consola):");

        JPanel panelNorte = new JPanel(new GridLayout(1, 2));
        panelNorte.add(lblEntrada);
        panelNorte.add(lblSalida);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 0));
        panelCentro.add(scrollInput);
        panelCentro.add(scrollOutput);

        JPanel panelControlesCentrales = new JPanel();
        panelControlesCentrales.add(sLexico);
        panelControlesCentrales.add(sSintactico);
        panelControlesCentrales.add(run);

        JPanel panelBotoneraDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBotoneraDerecha.add(btnGenerarAST);
        panelBotoneraDerecha.add(btnGenerarLLVM);
        panelBotoneraDerecha.add(btnGuardar);

        JPanel panelSur = new JPanel(new BorderLayout(10, 0));
        panelSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panelSur.add(btnCargar, BorderLayout.WEST);
        panelSur.add(panelControlesCentrales, BorderLayout.CENTER);
        panelSur.add(panelBotoneraDerecha, BorderLayout.EAST);

        this.setLayout(new BorderLayout(10, 10));
        this.add(panelNorte, BorderLayout.NORTH);
        this.add(panelCentro, BorderLayout.CENTER);
        this.add(panelSur, BorderLayout.SOUTH);

        run.addActionListener(e -> ejecutarProceso());
        btnCargar.addActionListener(e -> cargarArchivo());
        btnGuardar.addActionListener(e -> guardarArchivo());
        btnGenerarAST.addActionListener(e -> generarASTInteractivos());
        btnGenerarLLVM.addActionListener(e -> generarLLVMInteractivos());
    }

    private void cargarArchivo() {
        JFileChooser chooser = new JFileChooser();
        int seleccion = chooser.showOpenDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                String contenido = Files.readString(archivo.toPath());
                input.setText(contenido);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarArchivo() {
        if (txtOutput.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay nada para guardar en la salida.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int seleccion = chooser.showSaveDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();

            if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            }

            try (PrintWriter out = new PrintWriter(archivo)) {
                out.println(txtOutput.getText());
                JOptionPane.showMessageDialog(this, "Archivo guardado con éxito.");
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ejecutarProceso() {
        txtOutput.setText("");
        String texto = input.getText();

        if (texto.trim().isEmpty()) {
            txtOutput.setText("Escriba algo en la entrada...");
            return;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;

        System.setOut(ps);
        System.setErr(ps);

        try {
            limpiarEstado(); //limpieza de Estado.
            if (sLexico.isSelected()) {
                System.out.println("=== INICIANDO ANÁLISIS LÉXICO ===");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));

                lexer.Token t;
                while ((t = lexico.yylex()) != null) {
                    System.out.println("Token: " + t.toString());
                    if ("FIN".equals(t.nombre)) break;
                }
                System.out.println("=== ANÁLISIS LÉXICO FINALIZADO ===");

            } else {
                System.out.println("=== INICIANDO ANÁLISIS SINTÁCTICO ===");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
                SymbolFactory sf = new ComplexSymbolFactory();
                parser.Parser p = new parser.Parser(lexico, sf);

                p.parse();
                
                if (!p.erroresSemanticos.isEmpty()) {
                    System.err.println("\n=== ERRORES SEMÁNTICOS ENCONTRADOS ===");
                    for (String error : p.erroresSemanticos) {
                        System.err.println(error);
                    }
                    System.err.println("Total: " + p.erroresSemanticos.size() + " error(es).");
                    return;  // no generar AST ni LLVM
                }

                System.out.println("Resultado: ¡Análisis sintáctico correcto!");

                if (p.tablaSimbolos != null) {
                    System.out.println("\n--- TABLA DE SÍMBOLOS ---");
                    p.tablaSimbolos.print();

                    // Escritura automática en la raíz de ejecución de la app
                    System.out.println("\n[Guardando Tabla de Símbolos en raíz...]");
                    p.tablaSimbolos.escribirArchivo("ts.txt");
                    System.out.println("Archivo 'ts.txt' generado exitosamente en la raíz.");
                }
            }
        } catch (Exception e) {
            System.out.println("\n[!] ERROR:");
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            System.out.flush();
            System.err.flush();
            System.setOut(oldOut);
            System.setErr(oldErr);
            txtOutput.setText(baos.toString());
        }
    }

    private void generarASTInteractivos() {
        String texto = input.getText();
        if (texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La entrada está vacía.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Guardado directo en la raíz sin preguntar
        String raizJar = System.getProperty("user.dir");
        File destinoPng = new File(raizJar, "arbol.png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        System.setOut(ps); System.setErr(ps);

        try {
            limpiarEstado(); //limpieza de Estado.
            System.out.println("=== COMPILANDO PARA EXPORTAR AST ===");
            lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
            SymbolFactory sf = new ComplexSymbolFactory();
            parser.Parser p = new parser.Parser(lexico, sf);

            Programa astRoot = (Programa) p.parse().value;
            if (!p.erroresSemanticos.isEmpty()) {
                System.err.println("\n=== ERRORES SEMÁNTICOS ENCONTRADOS ===");
                for (String error : p.erroresSemanticos) {
                    System.err.println(error);
                }
                System.err.println("Total: " + p.erroresSemanticos.size() + " error(es).");
                return;  // no generar AST ni LLVM
            }

            if (astRoot != null) {
                // Guardamos el .dot temporal en la misma raíz
                File tempDot = new File(raizJar, "temp_arbol.dot");
                try (PrintWriter grafico = new PrintWriter(new FileWriter(tempDot))) {
                    grafico.println(astRoot.graficar());
                }

                System.out.println("Llamando a Graphviz para estructurar la imagen...");
                Process process = Runtime.getRuntime().exec(new String[]{
                        "dot", "-Tpng", tempDot.getAbsolutePath(), "-o", destinoPng.getAbsolutePath()
                });

                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("Graphviz Warning/Error: " + line);
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    System.out.println("Imagen del AST exportada con éxito en:\n" + destinoPng.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "¡Gráfico del AST generado con éxito como 'arbol.png'!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    System.err.println("Graphviz falló con código de salida: " + exitCode);
                }

                tempDot.delete();
            }
        } catch (Exception e) {
            System.err.println("Error procesando o renderizando el AST:");
            e.printStackTrace(System.out);
        } finally {
            System.out.flush(); System.err.flush();
            System.setOut(oldOut); System.setErr(oldErr);
            txtOutput.setText(baos.toString());
        }
    }

    private void generarLLVMInteractivos() {
        String texto = input.getText();
        if (texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La entrada está vacía.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Guardado directo en la raíz con el nombre "programa"
        String raizJar = System.getProperty("user.dir");
        File destinoLl = new File(raizJar, "programa.ll");
        String pathAbsolutoExe = new File(raizJar, "programa.exe").getAbsolutePath();
        String pathAbsolutoO = new File(raizJar, "programa.o").getAbsolutePath();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        System.setOut(ps); System.setErr(ps);

        try {
            limpiarEstado(); //limpieza de Estado.
            System.out.println("=== COMPILANDO PARA GENERAR LLVM IR ===");
            lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
            SymbolFactory sf = new ComplexSymbolFactory();
            parser.Parser p = new parser.Parser(lexico, sf);

            Programa astRoot = (Programa) p.parse().value;
            if (!p.erroresSemanticos.isEmpty()) {
                System.err.println("\n=== ERRORES SEMÁNTICOS ENCONTRADOS ===");
                for (String error : p.erroresSemanticos) {
                    System.err.println(error);
                }
                System.err.println("Total: " + p.erroresSemanticos.size() + " error(es).");
                return;  // no generar AST ni LLVM
            }

            if (astRoot != null) {
                // Guardamos automáticamente la TS al emitir LLVM
                if (p.tablaSimbolos != null) {
                    System.out.println("[Guardando Tabla de Símbolos en raíz...]");
                    p.tablaSimbolos.escribirArchivo("ts.txt");
                }

                System.out.println("Escribiendo instrucciones IR estructuradas...");
                try (PrintWriter codigoLLVM = new PrintWriter(new FileWriter(destinoLl))) {
                    codigoLLVM.println(astRoot.generarCodigo());
                    codigoLLVM.flush();
                }
                System.out.println("Código intermedio (.ll) guardado exitosamente en:\n" + destinoLl.getAbsolutePath());

                File archivoAlu = new File(raizJar + File.separator + "Funcion" + File.separator + "array_alu.ll");
                File archivoScanfO = new File(raizJar + File.separator + "Funcion" + File.separator + "scanf.o");

                System.out.println("Verificando dependencias en la carpeta '/Funcion'...");

                if (!archivoAlu.exists() ||!archivoScanfO.exists()) {
                    System.err.println("[!] ERROR CRÍTICO: Falta 'array_alu.ll' o 'scanf.o' en la ruta /Funcion.");
                    System.err.println("Asegúrate de que ambos archivos existan en: " + raizJar + File.separator + "Funcion");
                    JOptionPane.showMessageDialog(this, "Faltan componentes en la carpeta Funcion/ (Se requieren array_alu.ll y scanf.o)", "Error de Dependencias", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // -----------------------------------------------------------------
                // PASO A: clang -c -o programa.o programa.ll
                // -----------------------------------------------------------------
                System.out.println("\nPaso 1/2 (Clang): Traduciendo código intermedio a objeto (.o)...");
                String[] comandoPasoA = {
                        "clang", "-c",
                        "-o", pathAbsolutoO,
                        destinoLl.getAbsolutePath()
                };

                Process procPasoA = Runtime.getRuntime().exec(comandoPasoA);

                try (BufferedReader errorA = new BufferedReader(new InputStreamReader(procPasoA.getErrorStream()))) {
                    String lineaError;
                    while ((lineaError = errorA.readLine()) != null) {
                        System.err.println("Clang (Paso A) Log: " + lineaError);
                    }
                }

                int statusA = procPasoA.waitFor();
                if (statusA != 0) {
                    System.err.println("[!] El Paso A falló de forma crítica. Enlace abortado.");
                    JOptionPane.showMessageDialog(this, "Error de sintaxis LLVM IR. Clang no pudo crear el código objeto.", "Error (Paso A)", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // -----------------------------------------------------------------
                // PASO B: clang -o programa.exe programa.o array_alu.ll scanf.o
                // -----------------------------------------------------------------
                System.out.println("Paso 2/2 (Clang): Vinculando objetos y librerías de la cátedra...");
                String[] comandoPasoB = {
                        "clang",
                        "-o", pathAbsolutoExe,
                        pathAbsolutoO,
                        archivoAlu.getAbsolutePath(),
                        archivoScanfO.getAbsolutePath(),
                        "-lmsvcrt",
                        "-llegacy_stdio_definitions"
                };

                Process procPasoB = Runtime.getRuntime().exec(comandoPasoB);

                try (BufferedReader errorB = new BufferedReader(new InputStreamReader(procPasoB.getErrorStream()))) {
                    String lineaError;
                    while ((lineaError = errorB.readLine()) != null) {
                        System.err.println("Clang (Paso B) Log: " + lineaError);
                    }
                }

                int statusB = procPasoB.waitFor();
                if (statusB == 0) {
                    System.out.println("\n=== COMPILACIÓN COMPLETADA CON ÉXITO ===");
                    System.out.println("Ejecutable generado en: " + pathAbsolutoExe);
                    JOptionPane.showMessageDialog(this, "¡Archivos programa.ll, programa.o, programa.exe y ts.txt creados con total éxito!", "Éxito de Compilación", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    System.err.println("\n[!] El enlazador (Paso B) abortó el proceso. Código de salida: " + statusB);
                    JOptionPane.showMessageDialog(this, "El enlazador de Clang falló. Verificá que los prototipos de scanf coincidan.", "Error (Paso B)", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fatal emitiendo código o ejecutando Clang:");
            e.printStackTrace(System.out);
        } finally {
            System.out.flush(); System.err.flush();
            System.setOut(oldOut); System.setErr(oldErr);
            txtOutput.setText(baos.toString());
        }
    }
    private void limpiarEstado() {
        System.out.println("\n[SISTEMA] Iniciando limpieza de estado interno...");

        // 1. Invocamos la función nativa del parser que limpia todo el estado estático
        // (tablas de símbolos, listas de variables, contadores, etc.)
        parser.Parser.resetEstado();
        llvm.CodeGeneratorHelper.reset();
        
        // 2. Forzamos la liberación de memoria para destruir el AST anterior
        System.gc();

        System.out.println("[SISTEMA] Entorno limpio y listo para nueva compilación.\n");
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrueba().setVisible(true));
    }
}