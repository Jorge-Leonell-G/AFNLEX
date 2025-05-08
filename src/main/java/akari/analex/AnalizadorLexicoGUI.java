package akari.analex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class AnalizadorLexicoGUI extends JFrame implements ActionListener {

    private JTextArea inputTextArea;
    private JTextArea rulesTextArea;
    private JTextArea outputTextArea;
    private JTable afdTable;
    private DefaultTableModel afdTableModel;
    private JButton analyzeButton;
    private AnalizadorLexico analizador;
    private JButton loadAFDButton;
    private JButton animacionButton;
    private JButton volverButton;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel animacionPanel;
    private JPanel cintaPanel;
    private Timer animationTimer;
    private int currentAnimationIndex;

    public AnalizadorLexicoGUI() {
        super("Analizador Léxico con AFD");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);

        analizador = new AnalizadorLexico();
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Panel principal
        JPanel guiPrincipal = crearPanelPrincipal();
        mainPanel.add(guiPrincipal, "principal");

        // Panel de animación
        animacionPanel = crearPanelAnimacion();
        mainPanel.add(animacionPanel, "animacion");

        add(mainPanel);
        setVisible(true);
    }

    private JPanel crearPanelPrincipal() {
        JPanel panel = new JPanel(new BorderLayout());

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
        animacionButton = new JButton("Mostrar cinta de animación");
        animacionButton.addActionListener(this);

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
        buttonPanel.add(animacionButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(bottomPanel, BorderLayout.CENTER);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(rulesPanel, BorderLayout.CENTER);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel crearPanelAnimacion() {
    JPanel panel = new JPanel(new BorderLayout());
    
    // Panel para la cinta de animación (ahora más pequeño)
    cintaPanel = new JPanel();
    cintaPanel.setBorder(BorderFactory.createTitledBorder("Animación en Tiempo Real"));
    cintaPanel.setLayout(new BoxLayout(cintaPanel, BoxLayout.X_AXIS));
    cintaPanel.setPreferredSize(new Dimension(600, 50)); // Tamaño reducido
    
    // Botón para regresar
    volverButton = new JButton("Regresar a GUI Principal");
    volverButton.addActionListener(e -> {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        cardLayout.show(mainPanel, "principal");
    });
    
    // Panel contenedor para mejor organización
    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.add(new JScrollPane(cintaPanel), BorderLayout.CENTER);
    contentPanel.add(volverButton, BorderLayout.SOUTH);
    
    panel.add(contentPanel, BorderLayout.CENTER);
    
    return panel;
}

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == analyzeButton) {
            analizarEntrada();
        } else if (e.getSource() == loadAFDButton) {
            cargarAFD();
        } else if (e.getSource() == animacionButton) {
            if (analizador.analizadorAFD != null && !inputTextArea.getText().isEmpty()) {
                mostrarAnimacionCompleta(analizador.analizadorAFD, inputTextArea.getText());
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Primero cargue un AFD e ingrese texto para analizar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
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
            mostrarReglasLexicas();
            List<Token> tokens = analizador.analizarCadena(input);
            
            if (tokens.isEmpty()) {
                outputTextArea.append("No se encontraron tokens válidos en la entrada\n");
            } else {
                outputTextArea.append("--- Tokens encontrados ---\n");
                for (Token token : tokens) {
                    outputTextArea.append(token.toString() + "\n");
                }
                outputTextArea.append("\n");
            }

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
                    String patron = obtenerPatronParaEstado(estado, afd);
                    reglas.append(tipoToken).append(" = ").append(patron).append("\n");
                }
            }
        }
        
        rulesTextArea.setText(reglas.toString());
    }

    private String obtenerPatronParaEstado(int estadoAceptacion, AFD afd) {
        if (!afd.getStates().contains(estadoAceptacion) || !afd.isAccepting(estadoAceptacion)) {
            return "estado_no_aceptacion";
        }

        if (estadoAceptacion == afd.getStartState()) {
            return "ε";
        }

        Map<Integer, String> patrones = new HashMap<>();
        Queue<Integer> cola = new LinkedList<>();
        Set<Integer> visitados = new HashSet<>();

        cola.add(estadoAceptacion);
        patrones.put(estadoAceptacion, "");
        visitados.add(estadoAceptacion);

        while (!cola.isEmpty()) {
            int estadoActual = cola.poll();
            String patronActual = patrones.get(estadoActual);

            for (int estadoOrigen : afd.getStates()) {
                Map<Character, Integer> transiciones = afd.getTransitions(estadoOrigen);
                if (transiciones != null) {
                    for (Map.Entry<Character, Integer> trans : transiciones.entrySet()) {
                        if (trans.getValue() == estadoActual && !visitados.contains(estadoOrigen)) {
                            String nuevoPatron = trans.getKey() + patronActual;
                            
                            if (estadoOrigen == afd.getStartState()) {
                                return nuevoPatron;
                            }
                            
                            patrones.put(estadoOrigen, nuevoPatron);
                            cola.add(estadoOrigen);
                            visitados.add(estadoOrigen);
                        }
                    }
                }
            }
        }

        String patronMasCorto = null;
        for (Map.Entry<Integer, String> entry : patrones.entrySet()) {
            if (patronMasCorto == null || entry.getValue().length() < patronMasCorto.length()) {
                patronMasCorto = entry.getValue();
            }
        }

        return patronMasCorto != null ? patronMasCorto : "patrón_no_reconocido";
    }

    private void cargarAFD() {
        JFileChooser fileChooser = new JFileChooser();
        
        try {
            String rutaPredefinida = "C:\\Users\\leone\\Documents\\Netbeans Projects\\AnaLex\\afd_outputs_no_format";
            File dirPredefinido = new File(rutaPredefinida);
            
            if (!dirPredefinido.exists()) {
                boolean creado = dirPredefinido.mkdirs();
                if (!creado) {
                    outputTextArea.append("Advertencia: No se pudo crear la carpeta especificada. Usando directorio por defecto.\n");
                }
            }
            
            if (dirPredefinido.exists()) {
                fileChooser.setCurrentDirectory(dirPredefinido);
            } else {
                outputTextArea.append("Advertencia: La ruta especificada no existe. Usando directorio por defecto.\n");
            }
        } catch (Exception e) {
            outputTextArea.append("Advertencia: No se pudo establecer la ruta predefinida. Usando directorio por defecto.\n");
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                AFD afdCargado = cargarAFDDesdeArchivo(file);

                outputTextArea.setText("");
                analizador.cargarAFD(afdCargado);
                updateAFDTable(afdCargado);
                outputTextArea.setText("AFD cargado exitosamente desde archivo: " + file.getName() + "\n");
                mostrarReglasLexicas();

            } catch (Exception ex) {
                outputTextArea.setText("Error al cargar el AFD desde archivo.\n" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private AFD cargarAFDDesdeArchivo(File file) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (AFD) ois.readObject();
        }
    }

    private void updateAFDTable(AFD afd) {
        afdTableModel.setRowCount(0);
        afdTableModel.setColumnCount(0);

        if (afd == null || afd.getStates() == null || afd.getStates().isEmpty()) {
            afdTableModel.addColumn("Sin AFD");
            return;
        }

        Set<Character> alphabet = new HashSet<>();
        for (int state : afd.getStates()) {
            Map<Character, Integer> transitions = afd.getTransitions(state);
            if (transitions != null) {
                alphabet.addAll(transitions.keySet());
            }
        }
        List<Character> sortedAlphabet = new ArrayList<>(alphabet);
        Collections.sort(sortedAlphabet);

        afdTableModel.addColumn("Estado");
        for (char symbol : sortedAlphabet) {
            afdTableModel.addColumn(String.valueOf(symbol));
        }
        afdTableModel.addColumn("Aceptación");
        afdTableModel.addColumn("Tipo");

        List<Integer> sortedStates = new ArrayList<>(afd.getStates());
        Collections.sort(sortedStates);

        for (int state : sortedStates) {
            Object[] row = new Object[sortedAlphabet.size() + 3];
            row[0] = state;
            Map<Character, Integer> transitions = afd.getTransitions(state);
            for (int i = 0; i < sortedAlphabet.size(); i++) {
                char symbol = sortedAlphabet.get(i);
                row[i + 1] = (transitions != null && transitions.containsKey(symbol)) ? transitions.get(symbol) : "";
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

        outputTextArea.append("--- Animación del Análisis ---\n");
        outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());

        new Thread(() -> {
            int currentState = afd.getStartState();
            int position = 0;
            
            while (position < input.length()) {
                char currentChar = input.charAt(position);
                int nextState = afd.getNextState(currentState, currentChar);
                
                // Crear variables locales efectivamente finales (para evitar error en la funcion lambda)
                final char charToPrint = currentChar;
                final int stateToPrint = currentState;

                SwingUtilities.invokeLater(() -> {
                    outputTextArea.append("Carácter: '" + charToPrint + 
                                       "', Estado actual: " + stateToPrint);
                    
                    if (nextState != -1) {
                        outputTextArea.append(" -> Estado siguiente: " + nextState + "\n");
                    } else {
                        outputTextArea.append(" -> No hay transición para '" + currentChar + "'\n");
                    }
                    outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
                });

                if (nextState != -1) {
                    currentState = nextState;
                    position++;
                } else {
                    position++;
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            
            SwingUtilities.invokeLater(() -> {
                outputTextArea.append("--- Fin de la Animación ---\n");
                outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
            });
        }).start();
    }

    private void mostrarAnimacionCompleta(AFD afd, String input) {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        cintaPanel.removeAll();
        cintaPanel.setLayout(new BoxLayout(cintaPanel, BoxLayout.X_AXIS));
        
        if (afd == null || input == null || input.isEmpty()) {
            cintaPanel.add(new JLabel("No hay datos para mostrar"));
            cintaPanel.revalidate();
            cintaPanel.repaint();
            return;
        }
        
        List<JPanel> elementosAnimacion = new ArrayList<>();
        int currentState = afd.getStartState();
        
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            int nextState = afd.getNextState(currentState, currentChar);
            
            JPanel charPanel = new JPanel(new BorderLayout());
            charPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            charPanel.setPreferredSize(new Dimension(30, 30));
            
            JLabel charLabel = new JLabel(String.valueOf(currentChar), SwingConstants.CENTER);
            charLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
            
            Color bgColor;
            if (nextState == -1) {
                bgColor = Color.RED;
                charLabel.setForeground(Color.WHITE);
            } else if (afd.isAccepting(nextState)) {
                bgColor = Color.GREEN;
            } else {
                bgColor = Color.YELLOW;
            }
            charPanel.setBackground(bgColor);
            charPanel.add(charLabel, BorderLayout.CENTER);
            
            JLabel stateLabel = new JLabel("q"+currentState, SwingConstants.CENTER);
            stateLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            charPanel.add(stateLabel, BorderLayout.SOUTH);
            
            elementosAnimacion.add(charPanel);
            currentState = nextState != -1 ? nextState : afd.getStartState();
        }
        
        for (JPanel panel : elementosAnimacion) {
            cintaPanel.add(panel);
        }
        
        currentAnimationIndex = 0;
        animationTimer = new Timer(500, e -> {
            if (currentAnimationIndex > 0) {
                elementosAnimacion.get(currentAnimationIndex-1).setBackground(
                    elementosAnimacion.get(currentAnimationIndex-1).getBackground().darker());
            }
            
            if (currentAnimationIndex < elementosAnimacion.size()) {
                elementosAnimacion.get(currentAnimationIndex).setBackground(Color.CYAN);
                currentAnimationIndex++;
            } else {
                animationTimer.stop();
            }
            cintaPanel.revalidate();
            cintaPanel.repaint();
        });
        
        cintaPanel.revalidate();
        cintaPanel.repaint();
        cardLayout.show(mainPanel, "animacion");
        animationTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorLexicoGUI());
    }
}