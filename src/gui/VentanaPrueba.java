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
    private JButton btnGenerarAST;  // Nuevo
    private JButton btnGenerarLLVM; // Nuevo
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
        this.setSize(950, 550); // Un poco más ancha para acomodar la botonera derecha
        this.setLocationRelativeTo(null);

        grupoOpciones = new ButtonGroup();
        grupoOpciones.add(sLexico);
        grupoOpciones.add(sSintactico);
        sSintactico.setSelected(true); // Por defecto sintáctico, ya que habilita el AST y LLVM
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

        // Inicializar botones
        btnCargar = new JButton("Cargar Archivo");
        btnGuardar = new JButton("Guardar Salida");
        btnGenerarAST = new JButton("Generar AST");   // Nuevo
        btnGenerarLLVM = new JButton("Generar .ll");  // Nuevo

        JLabel lblEntrada = new JLabel("Entrada:");
        JLabel lblSalida = new JLabel("Salida (Consola):");

        JPanel panelNorte = new JPanel(new GridLayout(1, 2));
        panelNorte.add(lblEntrada);
        panelNorte.add(lblSalida);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 0));
        panelCentro.add(scrollInput);
        panelCentro.add(scrollOutput);

        // Panel para los controles centrales (RadioButtons y Ejecutar)
        JPanel panelControlesCentrales = new JPanel();
        panelControlesCentrales.add(sLexico);
        panelControlesCentrales.add(sSintactico);
        panelControlesCentrales.add(run);

        // 🌟 NUEVO: Panel contenedor para agrupar los botones en el extremo derecho
        JPanel panelBotoneraDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBotoneraDerecha.add(btnGenerarAST);
        panelBotoneraDerecha.add(btnGenerarLLVM);
        panelBotoneraDerecha.add(btnGuardar);

        // Panel Sur con BorderLayout
        JPanel panelSur = new JPanel(new BorderLayout(10, 0));
        panelSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        panelSur.add(btnCargar, BorderLayout.WEST);
        panelSur.add(panelControlesCentrales, BorderLayout.CENTER);
        panelSur.add(panelBotoneraDerecha, BorderLayout.EAST); // Colocamos la botonera aquí

        this.setLayout(new BorderLayout(10, 10));
        this.add(panelNorte, BorderLayout.NORTH);
        this.add(panelCentro, BorderLayout.CENTER);
        this.add(panelSur, BorderLayout.SOUTH);

        // Acciones
        run.addActionListener(e -> ejecutarProceso());
        btnCargar.addActionListener(e -> cargarArchivo());
        btnGuardar.addActionListener(e -> guardarArchivo());
        btnGenerarAST.addActionListener(e -> generarASTInteractivos());   // Asignar acción AST
        btnGenerarLLVM.addActionListener(e -> generarLLVMInteractivos()); // Asignar acción LLVM
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

                System.out.println("Resultado: ¡Análisis sintáctico correcto!");

                if (p.tablaSimbolos != null) {
                    System.out.println("\n--- TABLA DE SÍMBOLOS ---");
                    p.tablaSimbolos.print();
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

    // 🌟 NUEVO MÉTODO: Compila el código, genera el .dot y pide guardar la imagen .png final
    private void generarASTInteractivos() {
        String texto = input.getText();
        if (texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La entrada está vacía.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccione dónde guardar el gráfico del AST (.png)");
        int seleccion = chooser.showSaveDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File destinoPng = chooser.getSelectedFile();
            if (!destinoPng.getName().toLowerCase().endsWith(".png")) {
                destinoPng = new File(destinoPng.getAbsolutePath() + ".png");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            System.setOut(ps); System.setErr(ps);

            try {
                System.out.println("=== COMPILANDO PARA EXPORTAR AST ===");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
                SymbolFactory sf = new ComplexSymbolFactory();
                parser.Parser p = new parser.Parser(lexico, sf);

                Programa astRoot = (Programa) p.parse().value;

                if (astRoot != null) {
                    // Generamos el archivo .dot temporal en la raíz del proyecto
                    File tempDot = new File("temp_arbol.dot");
                    try (PrintWriter grafico = new PrintWriter(new FileWriter(tempDot))) {
                        grafico.println(astRoot.graficar());
                    }

                    System.out.println("Llamando a Graphviz para estructurar la imagen...");
                    Process process = Runtime.getRuntime().exec(new String[]{
                            "dot", "-Tpng", tempDot.getAbsolutePath(), "-o", destinoPng.getAbsolutePath()
                    });

                    // Captura de errores de Graphviz
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            System.err.println("Graphviz Warning/Error: " + line);
                        }
                    }

                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        System.out.println("Imagen del AST exportada con éxito en:\n" + destinoPng.getAbsolutePath());
                        JOptionPane.showMessageDialog(this, "¡Gráfico del AST generado con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        System.err.println("Graphviz falló con código de salida: " + exitCode);
                    }

                    // Borramos el .dot temporal
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
    }

    // 🌟 NUEVO MÉTODO: Compila el código actual y exporta el código LLVM IR a la ubicación deseada
    private void generarLLVMInteractivos() {
        String texto = input.getText();
        if (texto.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "La entrada está vacía.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccione dónde guardar el código LLVM IR (.ll)");
        int seleccion = chooser.showSaveDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File destinoLl = chooser.getSelectedFile();
            if (!destinoLl.getName().toLowerCase().endsWith(".ll")) {
                destinoLl = new File(destinoLl.getAbsolutePath() + ".ll");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            PrintStream oldOut = System.out;
            PrintStream oldErr = System.err;
            System.setOut(ps); System.setErr(ps);

            try {
                System.out.println("=== COMPILANDO PARA GENERAR LLVM IR ===");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
                SymbolFactory sf = new ComplexSymbolFactory();
                parser.Parser p = new parser.Parser(lexico, sf);

                Programa astRoot = (Programa) p.parse().value;

                if (astRoot != null) {
                    System.out.println("Escribiendo instrucciones IR estructuradas...");
                    try (PrintWriter codigoLLVM = new PrintWriter(new FileWriter(destinoLl))) {
                        codigoLLVM.println(astRoot.generarCodigo());
                    }
                    System.out.println("Código intermedio (.ll) guardado exitosamente en:\n" + destinoLl.getAbsolutePath());
                    JOptionPane.showMessageDialog(this, "¡Archivo LLVM IR generado e impreso con éxito!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                System.err.println("Error fatal emitiendo el código intermedio LLVM:");
                e.printStackTrace(System.out);
            } finally {
                System.out.flush(); System.err.flush();
                System.setOut(oldOut); System.setErr(oldErr);
                txtOutput.setText(baos.toString());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrueba().setVisible(true));
    }
}