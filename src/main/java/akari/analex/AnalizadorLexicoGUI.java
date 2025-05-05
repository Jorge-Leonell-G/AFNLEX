package akari.analex;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AnalizadorLexicoGUI extends JFrame implements ActionListener {

    private JTextArea inputTextArea;
    private JTextArea rulesTextArea;
    private final JTextArea outputTextArea;
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
    // Limpiar completamente el área de salida
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
        // Mostrar reglas léxicas basadas en el AFD actual
        mostrarReglasLexicas();

        // Analizar la cadena (esto ahora es independiente de la animación)
        List<Token> tokens = analizador.analizarCadena(input);
        
        // Mostrar tokens encontrados
        if (tokens.isEmpty()) {
            outputTextArea.append("No se encontraron tokens válidos en la entrada\n");
        } else {
            outputTextArea.append("--- Tokens encontrados ---\n");
            for (Token token : tokens) {
                outputTextArea.append(token.toString() + "\n");
            }
            outputTextArea.append("\n"); // Espacio antes de la animación
        }

        // Mostrar animación del análisis (con el AFD actual)
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
    // Verificar si el estado es válido
    if (!afd.getStates().contains(estadoAceptacion) || !afd.isAccepting(estadoAceptacion)) {
        return "estado_no_aceptacion";
    }

    // Caso especial para estado inicial que sea de aceptación
    if (estadoAceptacion == afd.getStartState()) {
        return "ε"; // Cadena vacía
    }

    // Reconstruir el patrón mediante búsqueda en anchura inversa
    Map<Integer, String> patrones = new HashMap<>();
    Queue<Integer> cola = new LinkedList<>();
    Set<Integer> visitados = new HashSet<>();

    // Inicializar con el estado de aceptación
    cola.add(estadoAceptacion);
    patrones.put(estadoAceptacion, "");
    visitados.add(estadoAceptacion);

    while (!cola.isEmpty()) {
        int estadoActual = cola.poll();
        String patronActual = patrones.get(estadoActual);

        // Buscar todos los estados que llevan al estado actual
        for (int estadoOrigen : afd.getStates()) {
            Map<Character, Integer> transiciones = afd.getTransitions(estadoOrigen);
            if (transiciones != null) {
                for (Map.Entry<Character, Integer> trans : transiciones.entrySet()) {
                    if (trans.getValue() == estadoActual && !visitados.contains(estadoOrigen)) {
                        String nuevoPatron = trans.getKey() + patronActual;
                        
                        // Si llegamos al estado inicial, retornamos el patrón completo
                        if (estadoOrigen == afd.getStartState()) {
                            return nuevoPatron;
                        }
                        
                        // Almacenar el nuevo patrón y continuar la búsqueda
                        patrones.put(estadoOrigen, nuevoPatron);
                        cola.add(estadoOrigen);
                        visitados.add(estadoOrigen);
                    }
                }
            }
        }
    }

    // Si no se encontró un camino al estado inicial, buscar el patrón más corto
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
    
    // Configurar ruta predefinida específica
    try {
        String rutaPredefinida = "C:\\Users\\leone\\Documents\\Netbeans Projects\\AnaLex\\afd_outputs_no_format";
        File dirPredefinido = new File(rutaPredefinida);
        
        // Si la carpeta no existe, intentar crearla
        if (!dirPredefinido.exists()) {
            boolean creado = dirPredefinido.mkdirs();
            if (!creado) {
                outputTextArea.append("Advertencia: No se pudo crear la carpeta especificada. Usando directorio por defecto.\n");
            }
        }
        
        // Verificar nuevamente si existe después de intentar crearla
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
            java.io.File file = fileChooser.getSelectedFile();
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
            AFD afdCargado = (AFD) ois.readObject();
            ois.close();

            // Limpiar el área de salida al cargar nuevo AFD
            outputTextArea.setText("");
            
            // Cargar el nuevo AFD
            analizador.cargarAFD(afdCargado);
            updateAFDTable(afdCargado);
            
            // Mostrar mensaje de éxito
            outputTextArea.setText("AFD cargado exitosamente desde archivo: " + file.getName() + "\n");
            
            // Actualizar reglas léxicas mostradas
            mostrarReglasLexicas();

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

    // Limpiar cualquier animación previa y comenzar nueva
    outputTextArea.append("--- Animación del Análisis ---\n");
    outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());

    new Thread(() -> {
        int currentState = afd.getStartState();
        int position = 0;
        
        while (position < input.length()) {
            char currentChar = input.charAt(position);
            int nextState = afd.getNextState(currentState, currentChar);

            // Crear variables efectivamente finales
            final char charToPrint = currentChar;
            final int stateToPrint = currentState;

            SwingUtilities.invokeLater(() -> {
                outputTextArea.append("Carácter: '" + charToPrint +
                          "', Estado actual: " + stateToPrint + "\n");
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
                // Carácter no reconocido - avanzar de todos modos
                position++;
            }

            try {
                Thread.sleep(500); // Pausa para la animación
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnalizadorLexicoGUI());
    }
}