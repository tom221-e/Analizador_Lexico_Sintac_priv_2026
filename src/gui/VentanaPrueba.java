package gui;

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
    private JButton btnCargar;  // Nuevo
    private JButton btnGuardar; // Nuevo
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
        this.setSize(850, 550); // Un poco más ancha para los nuevos botones
        this.setLocationRelativeTo(null);

        grupoOpciones = new ButtonGroup();
        grupoOpciones.add(sLexico);
        grupoOpciones.add(sSintactico);
        sLexico.setSelected(true);
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

        // Inicializar nuevos botones
        btnCargar = new JButton("Cargar Archivo");
        btnGuardar = new JButton("Guardar Salida");

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

        // Panel Sur con BorderLayout para separar los botones a los extremos
        JPanel panelSur = new JPanel(new BorderLayout(10, 0));
        panelSur.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Margen interno
        panelSur.add(btnCargar, BorderLayout.WEST);
        panelSur.add(panelControlesCentrales, BorderLayout.CENTER);
        panelSur.add(btnGuardar, BorderLayout.EAST);

        this.setLayout(new BorderLayout(10, 10));
        this.add(panelNorte, BorderLayout.NORTH);
        this.add(panelCentro, BorderLayout.CENTER);
        this.add(panelSur, BorderLayout.SOUTH);

        // Acciones
        run.addActionListener(e -> ejecutarProceso());
        btnCargar.addActionListener(e -> cargarArchivo());
        btnGuardar.addActionListener(e -> guardarArchivo());
    }

    private void cargarArchivo() {
        JFileChooser chooser = new JFileChooser();
        int seleccion = chooser.showOpenDialog(this);

        if (seleccion == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                // Leer todo el contenido y ponerlo en el input
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

            // Asegurar que tenga extensión .txt si el usuario no la puso
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

        // Redirigir System.out y System.err al JTextArea
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;

        System.setOut(ps);
        System.setErr(ps); // Importante para ver errores de CUP

        try {
            if (sLexico.isSelected()) {
                System.out.println("=== INICIANDO ANÁLISIS LÉXICO ===");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));

                lexer.Token t;
                // ESTE ES EL BUCLE QUE FALTABA:
                // Llamamos a yylex() hasta que devuelva null o un token de fin
                while ((t = lexico.yylex()) != null) {
                    System.out.println("Token: " + t.toString());

                    // Si tienes un token llamado FIN o EOF, rompemos el bucle
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
            e.printStackTrace(System.out); // Esto ahora saldrá en el JTextArea
        } finally {
            System.out.flush();
            System.err.flush();
            System.setOut(oldOut);
            System.setErr(oldErr);
            txtOutput.setText(baos.toString()); // Volcar todo lo capturado al GUI
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrueba().setVisible(true));
    }
}