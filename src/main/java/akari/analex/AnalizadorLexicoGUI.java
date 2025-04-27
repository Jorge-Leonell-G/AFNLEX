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
        rulesPanel.add(new JScrollPane(rulesTextArea), BorderLayout.CENTER);

        // Botón de análisis
        analyzeButton = new JButton("Analizar");
        analyzeButton.addActionListener(this);

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
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.add(outputPanel);
        bottomPanel.add(afdTablePanel);

        add(inputPanel, BorderLayout.NORTH);
        add(rulesPanel, BorderLayout.CENTER);
        add(analyzeButton, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH); // Reemplazado BOTTOM con SOUTH para el botón

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == analyzeButton) {
            outputTextArea.setText("");
            analizador = new AnalizadorLexico(); // Reiniciar el analizador para nuevas reglas

            // Parsear las reglas del área de texto
            String[] rules = rulesTextArea.getText().split("\n");
            for (String rule : rules) {
                String[] parts = rule.split("=", 2);
                if (parts.length == 2) {
                    analizador.agregarReglaLexica(parts[0].trim(), parts[1].trim());
                } else if (!rule.trim().isEmpty()) {
                    outputTextArea.append("Error en la regla: " + rule + "\n");
                }
            }

            // Construir el AFD
            analizador.unionAnalizadorLexico(analizador.obtenerReglasLexicas());
            AFD afd = analizador.analizadorAFD;

            if (afd != null) {
                // Mostrar la tabla del AFD
                updateAFNTable(afd);

                // Analizar el texto de entrada
                List<Token> tokens = analizador.analizarCadena(inputTextArea.getText());
                for (Token token : tokens) {
                    outputTextArea.append(token.toString() + "\n");
                }

                // Aquí podríamos implementar una animación básica del análisis
                // mostrando el estado actual del AFD mientras consume la entrada.
                animateAnalysis(afd, inputTextArea.getText());

            } else {
                outputTextArea.append("No se pudo construir el AFD.\n");
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