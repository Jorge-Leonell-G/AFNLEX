/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akari.analex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Akari Aguilera
 */
 public class AnalizadorLexicoGUI extends JFrame implements ActionListener {

    private JTextArea inputTextArea;
    private JTextArea rulesTextArea;
    private JTextArea outputTextArea;
    private JTable afdTable;
    private DefaultTableModel afdTableModel;
    private JButton analyzeButton;
    private AnalizadorLexico analizador;
    private JButton loadAFDButton;

    public AnalizadorLexicoGUI() {
    super("Analizador Léxico con AFD");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(800, 600);
    setLayout(new BorderLayout());

    analizador = new AnalizadorLexico();

    // Panel de entrada
    JPanel inputPanel = new JPanel(new BorderLayout());
    inputPanel.setBorder(BorderFactory.createTitledBorder("Texto de Entrada"));
    inputTextArea = new JTextArea();
    inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);

    // Panel de reglas
    JPanel rulesPanel = new JPanel(new BorderLayout());
    rulesPanel.setBorder(BorderFactory.createTitledBorder("Reglas Léxicas (tipo=patrón, una por línea)"));
    rulesTextArea = new JTextArea();
    rulesTextArea.setEditable(false); // Solo visualización
    rulesTextArea.setText("Reglas cargadas desde archivo aparecerán aquí.");
    // Agregamos las reglas por defecto aquí
/*
    rulesTextArea.setText(
            "PALABRA_CLAVE=if|else\n" +
            "IDENTIFICADOR=[a-zA-Z][a-zA-Z0-9]*\n" +
            "NUMERO=[0-9]+\n" +
            "OPERADOR=\\+|\\-|\\*|/\n" +
            "ESPACIO=\\s+"
    );
    rulesPanel.add(new JScrollPane(rulesTextArea), BorderLayout.CENTER);
*/
    // Botón de análisis
    analyzeButton = new JButton("Analizar");
    analyzeButton.addActionListener(this);
    
    loadAFDButton = new JButton("Cargar AFD desde archivo");
    loadAFDButton.addActionListener(this);

    // Panel de salida
    JPanel outputPanel = new JPanel(new BorderLayout());
    outputPanel.setBorder(BorderFactory.createTitledBorder("Tokens Encontrados"));
    outputTextArea = new JTextArea();
    outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);

    // Tabla del AFD
    JPanel afdTablePanel = new JPanel(new BorderLayout());
    afdTablePanel.setBorder(BorderFactory.createTitledBorder("Tabla de Transiciones del AFD"));
    afdTableModel = new DefaultTableModel();
    afdTable = new JTable(afdTableModel);
    afdTablePanel.add(new JScrollPane(afdTable), BorderLayout.CENTER);

    // Panel principal inferior
    
    //JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
    /*
    bottomPanel.add(outputPanel);
    bottomPanel.add(afdTablePanel);

    add(inputPanel, BorderLayout.NORTH);
    add(rulesPanel, BorderLayout.CENTER);
    add(analyzeButton, BorderLayout.SOUTH);
    add(bottomPanel, BorderLayout.SOUTH);
    */
    
    // Panel principal inferior
    JPanel bottomPanel = new JPanel(new GridLayout(1, 2));

    // Agregar los dos subpaneles al bottomPanel
    bottomPanel.add(outputPanel);
    bottomPanel.add(afdTablePanel);
    
    JPanel buttonPanel = new JPanel(new FlowLayout());
    buttonPanel.add(analyzeButton);
    buttonPanel.add(loadAFDButton);

    // Crear un nuevo panel para el botón y el bottomPanel
    JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.add(buttonPanel, BorderLayout.NORTH);
    southPanel.add(bottomPanel, BorderLayout.CENTER);

    // Agregar los paneles principales al frame
    add(inputPanel, BorderLayout.NORTH);
    add(rulesPanel, BorderLayout.CENTER);
    add(southPanel, BorderLayout.SOUTH);
    
    

    setVisible(true);
}


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loadAFDButton) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File file = fileChooser.getSelectedFile();
                    java.io.FileInputStream fis = new java.io.FileInputStream(file);
                    java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
                    AFD afdCargado = (AFD) ois.readObject();
                    ois.close();

                    analizador = new AnalizadorLexico(); // Reiniciar analizador
                    analizador.cargarAFD(afdCargado);

                    updateAFNTable(afdCargado);
                    outputTextArea.setText("AFD cargado exitosamente desde archivo: " + file.getName() + "\n");

                    // Mostrar reglas del AFD cargado
                    StringBuilder reglas = new StringBuilder();
                    for (int estado : afdCargado.getStates()) {
                        if (afdCargado.isAccepting(estado)) {
                            String tipoToken = afdCargado.getTokenType(estado);
                            if (tipoToken != null) {
                                reglas.append(tipoToken).append(" = patrón_desconocido\n"); // puedes adaptar este texto
                            }
                        }
                    }
                    rulesTextArea.setText(reglas.toString().isEmpty() ? "No se encontraron reglas." : reglas.toString());

                } catch (Exception ex) {
                    ex.printStackTrace();
                    outputTextArea.setText("Error al cargar el AFD desde archivo.\n" + ex.getMessage());
                }
            }
        }
    }

    private void updateAFNTable(AFD afd) {
        afdTableModel.setRowCount(0);
        afdTableModel.setColumnCount(0);

        if (afd == null || afd.getStates() == null || afd.getStates().isEmpty()) {
            afdTableModel.addColumn("Sin AFD");
            return;
        }

        Set<Integer> states = afd.getStates();
        Set<Character> alphabet = new java.util.HashSet<>();
        for (int state : states) {
            Map<Character, Integer> transitions = afd.getTransitions(state);
            if (transitions != null) {
                alphabet.addAll(transitions.keySet());
            }
        }
        java.util.List<Character> sortedAlphabet = new java.util.ArrayList<>(alphabet);
        java.util.Collections.sort(sortedAlphabet);

        afdTableModel.addColumn("Estado");
        for (char symbol : sortedAlphabet) {
            afdTableModel.addColumn(String.valueOf(symbol));
        }
        afdTableModel.addColumn("Aceptación");
        afdTableModel.addColumn("Tipo");

        java.util.List<Integer> sortedStates = new java.util.ArrayList<>(states);
        java.util.Collections.sort(sortedStates);

        for (int state : sortedStates) {
            Object[] row = new Object[sortedAlphabet.size() + 3];
            row[0] = state;
            Map<Character, Integer> transitions = afd.getTransitions(state);
            for (int i = 0; i < sortedAlphabet.size(); i++) {
                char symbol = sortedAlphabet.get(i);
                if (transitions != null && transitions.containsKey(symbol)) {
                    row[i + 1] = transitions.get(symbol);
                } else {
                    row[i + 1] = "";
                }
            }
            row[sortedAlphabet.size() + 1] = afd.isAccepting(state) ? "Sí" : "No";
            row[sortedAlphabet.size() + 2] = afd.getTokenType(state) != null ? afd.getTokenType(state) : "";
            afdTableModel.addRow(row);
        }
    }

    private void animateAnalysis(AFD afd, String input) {
        if (afd == null || input.isEmpty()) {
            return;
        }

        new Thread(() -> {
            int currentState = afd.getStartState();
            outputTextArea.append("\n--- Animación del Análisis ---\n");
            outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());

            for (int i = 0; i < input.length(); i++) {
                char currentChar = input.charAt(i);
                int nextState = afd.getNextState(currentState, currentChar);

                outputTextArea.append("Carácter: '" + currentChar + "', Estado actual: " + currentState);
                if (nextState != -1) {
                    outputTextArea.append(" -> Estado siguiente: " + nextState + "\n");
                    currentState = nextState;
                } else {
                    outputTextArea.append(" -> No hay transición para '" + currentChar + "'\n");
                    // Aquí podríamos detener la animación o continuar al siguiente posible token
                }
                outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());

                try {
                    Thread.sleep(500); // Pausa para la animación
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            outputTextArea.append("--- Fin de la Animación ---\n");
            outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorLexicoGUI());
    }
}