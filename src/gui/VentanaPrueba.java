package gui;

import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import javax.swing.*;
import java.awt.*;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.SymbolFactory;

public class VentanaPrueba extends javax.swing.JFrame {

    // Componentes de la interfaz
    private JTextPane input;
    private JTextArea txtOutput;
    private JRadioButton sLexico;
    private JRadioButton sSintactico;
    private JButton run;
    private JScrollPane scrollInput;
    private JScrollPane scrollOutput;
    private ButtonGroup grupoOpciones;

    public VentanaPrueba() {
        super("Analizador de Compiladores"); // Título de la ventana
        initComponents();
        configurarVentana();
    }

    private void configurarVentana() {
        // Configuración esencial para que se vea la ventana
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(700, 500);
        this.setLocationRelativeTo(null); // Centrar en pantalla

        // Agrupar RadioButtons para que solo se seleccione uno
        grupoOpciones = new ButtonGroup();
        grupoOpciones.add(sLexico);
        grupoOpciones.add(sSintactico);
        sLexico.setSelected(true);
    }

    private void initComponents() {
        // Inicializar componentes
        input = new JTextPane();
        scrollInput = new JScrollPane(input);

        txtOutput = new JTextArea();
        txtOutput.setEditable(false);
        txtOutput.setBackground(new Color(240, 240, 240));
        scrollOutput = new JScrollPane(txtOutput);

        sLexico = new JRadioButton("Léxico");
        sSintactico = new JRadioButton("Sintáctico");
        run = new JButton("Ejecutar");

        JLabel lblEntrada = new JLabel("Entrada:");
        JLabel lblSalida = new JLabel("Salida (Consola):");

        // Diseño (Layout)
        JPanel panelNorte = new JPanel(new GridLayout(1, 2));
        panelNorte.add(lblEntrada);
        panelNorte.add(lblSalida);

        JPanel panelCentro = new JPanel(new GridLayout(1, 2, 10, 0));
        panelCentro.add(scrollInput);
        panelCentro.add(scrollOutput);

        JPanel panelSur = new JPanel();
        panelSur.add(sLexico);
        panelSur.add(sSintactico);
        panelSur.add(run);

        // Agregar al contenedor principal
        this.setLayout(new BorderLayout(10, 10));
        this.add(panelNorte, BorderLayout.NORTH);
        this.add(panelCentro, BorderLayout.CENTER);
        this.add(panelSur, BorderLayout.SOUTH);

        // Acción del botón
        run.addActionListener(e -> ejecutarProceso());
    }

    private void ejecutarProceso() {
        txtOutput.setText(""); // Limpiar salida
        String texto = input.getText();

        if (texto.trim().isEmpty()) {
            txtOutput.setText("Escriba algo en la entrada...");
            return;
        }

        // Redirigir System.out al JTextArea
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream oldOut = System.out;
        System.setOut(ps);

        try {
            if (sLexico.isSelected()) {
                System.out.println("Iniciando Análisis Léxico...");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
                lexer.Token t;
                while ((t = lexico.yylex()) != null) {
                    System.out.println("Token: " + t);
                    if (t.nombre.equals("FIN")) break;
                }
            } else {
                System.out.println("Iniciando Análisis Sintáctico...");
                lexer.Lexer lexico = new lexer.Lexer(new StringReader(texto));
                SymbolFactory sf = new ComplexSymbolFactory();
                parser.Parser p = new parser.Parser(lexico, sf);

                p.parse();

                // Acceso seguro a tablaSimbolos
                if (p.tablaSimbolos != null) {
                    System.out.println("\n--- TABLA DE SÍMBOLOS ---");
                    p.tablaSimbolos.print();
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            System.out.flush();
            System.setOut(oldOut);
            txtOutput.setText(baos.toString()); // Volcar lo capturado al GUI
        }
    }

    public static void main(String[] args) {
        // Asegurar que la interfaz se inicie en el hilo correcto
        SwingUtilities.invokeLater(() -> {
            new VentanaPrueba().setVisible(true);
        });
    }
}