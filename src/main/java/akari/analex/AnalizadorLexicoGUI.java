package akari.analex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        rulesTextArea.setEditable(false);
        rulesPanel.add(new JScrollPane(rulesTextArea), BorderLayout.CENTER);

        // Botones
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

        // Panel inferior
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.add(outputPanel);
        bottomPanel.add(afdTablePanel);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(analyzeButton);
        buttonPanel.add(loadAFDButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(bottomPanel, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.NORTH);
        add(rulesPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == analyzeButton) {
            analizarEntrada();
        } else if (e.getSource() == loadAFDButton) {
            cargarAFD();
        }
    }

    private void analizarEntrada() {
        outputTextArea.setText("");
        
        if (analizador.analizadorAFD == null) {
            outputTextArea.append("Error: No hay un AFD cargado para analizar\n");
            return;
        }

        String input = inputTextArea.getText().trim();
        if (input.isEmpty()) {
            outputTextArea.append("Error: El texto de entrada está vacío\n");
            return;
        }

        try {
            // Mostrar reglas léxicas basadas en el AFD
            mostrarReglasLexicas();

            // Analizar la cadena
            List<Token> tokens = analizador.analizarCadena(input);
            
            if (tokens.isEmpty()) {
                outputTextArea.append("No se encontraron tokens válidos en la entrada\n");
            } else {
                outputTextArea.append("--- Tokens encontrados ---\n");
                for (Token token : tokens) {
                    outputTextArea.append(token.toString() + "\n");
                }
            }

            // Mostrar animación del análisis
            animateAnalysis(analizador.analizadorAFD, input);
            
        } catch (Exception ex) {
            outputTextArea.append("Error durante el análisis: " + ex.getMessage() + "\n");
            ex.printStackTrace();
        }
    }

    private void mostrarReglasLexicas() {
        StringBuilder reglas = new StringBuilder();
        AFD afd = analizador.analizadorAFD;
        
        for (int estado : afd.getStates()) {
            if (afd.isAccepting(estado)) {
                String tipoToken = afd.getTokenType(estado);
                if (tipoToken != null && !tipoToken.isEmpty()) {
                    // Obtener el patrón que lleva a este estado de aceptación
                    String patron = obtenerPatronParaEstado(estado, afd);
                    reglas.append(tipoToken).append(" = ").append(patron).append("\n");
                }
            }
        }
        
        rulesTextArea.setText(reglas.toString());
    }

    private String obtenerPatronParaEstado(int estadoAceptacion, AFD afd) {
        // Implementación simplificada - deberías adaptarla a tu estructura de AFD
        // Esta es una versión básica que funciona para el ejemplo A+
        if (estadoAceptacion == 2) {
            return "A+";
        }
        return "patrón_desconocido";
    }

    private void cargarAFD() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.io.FileInputStream fis = new java.io.FileInputStream(file);
                java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
                AFD afdCargado = (AFD) ois.readObject();
                ois.close();

                analizador.cargarAFD(afdCargado);
                updateAFDTable(afdCargado);
                outputTextArea.setText("AFD cargado exitosamente desde archivo: " + file.getName() + "\n");

            } catch (Exception ex) {
                outputTextArea.setText("Error al cargar el AFD desde archivo.\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void updateAFDTable(AFD afd) {
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