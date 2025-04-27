package akari.analex;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.regex.*;

/**
 *
 * @author
 */
public class menu extends JFrame {

    private DefaultListModel<String> afnListModel;
    private JList<String> afnList;
    private ArrayList<AFN> afnStorage; // Guarda los AFNs creados
    private int afnCounter = 1; // Para nombrar los AFNs automáticamente
    //private final String directoryPath = "AFNbasicos";

    public menu() {
        setTitle("Menu del programa de compiladores");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        afnStorage = new ArrayList<>();
        afnListModel = new DefaultListModel<>();
        afnList = new JList<>(afnListModel);
        afnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane listScrollPane = new JScrollPane(afnList);
        
        // Crear carpeta si no existe
        
        /*
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        */
        
        //Carga de manera inicial de los AFN
        //loadSavedAFNs();

        // Panel de botones
        JPanel buttonPanel = new JPanel();
        //buttonPanel.setLayout(new GridLayout(8, 1, 5, 5));
        buttonPanel.setLayout(new GridLayout(5, 2, 10, 10));

        JButton btnSymbol = new JButton("Crear AFN para un símbolo");
        JButton btnConcatenation = new JButton("Concatenar dos AFNs");
        JButton btnUnion = new JButton("Unir dos AFNs");
        JButton btnKleeneStar = new JButton("Aplicar Cerradura de Kleene");
        JButton btnPositiveClosure = new JButton("Aplicar Cerradura Positiva");
        JButton btnOptional = new JButton("Hacer AFN Opcional");
        JButton btnERtoAFN = new JButton("Convertir Expresión Regular a AFN");
        JButton btnDeleteAFN = new JButton("Eliminar AFN");
        JButton btnAFNtoAFD = new JButton("Convertir AFN a AFD");
        JButton btnLexicAnalyzer = new JButton("Analizador Léxico");
        JButton btnExit = new JButton("Salir");

        buttonPanel.add(btnSymbol);
        buttonPanel.add(btnConcatenation);
        buttonPanel.add(btnUnion);
        buttonPanel.add(btnKleeneStar);
        buttonPanel.add(btnPositiveClosure);
        buttonPanel.add(btnOptional);
        buttonPanel.add(btnERtoAFN);
        buttonPanel.add(btnDeleteAFN);
        buttonPanel.add(btnAFNtoAFD);
        buttonPanel.add(btnLexicAnalyzer);
        buttonPanel.add(btnExit);

        setLayout(new BorderLayout(10, 10));
        add(new JLabel("Lista de AFNs Guardados:"), BorderLayout.NORTH);
        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        // Acciones
        btnSymbol.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //expresion regular para validar que el simbolo sea adecuado
                Pattern symbolPattern = Pattern.compile("^[a-zA-Z]$");
                String symbol = JOptionPane.showInputDialog("Ingrese un símbolo:");
                if(symbol.length() != 1 || !symbolPattern.matcher(symbol).matches()){
                    JOptionPane.showMessageDialog(null, "Debe ingresar un solo símbolo alfabético (a-z o A-Z).");
                    return;
                }
                String tokenType = JOptionPane.showInputDialog("Ingrese el tipo de token (opcional):");
                if (symbol != null && !symbol.isEmpty()) {
                    AFN afn = new AFN();
                    afn = ThompsonAlgorithm.constructForSymbol(afn, symbol.charAt(0), tokenType);
                    addAFN(afn, "AFN_" + symbol + (afnCounter++));
                    afn.printAFN(); //salida en consola de la construccion en cadena de texto del AFN
                    //Guardado del archivo
                    //AFNFile.guardarAFN(afn, symbol, directoryPath);
                    //guardarAFNEnArchivo(afn, symbol);
                    JOptionPane.showMessageDialog(null, "AFN creado para el símbolo: " + symbol);
                }
            }
        });
        
        // Agregar el MouseListener para detectar el doble clic en la lista
        afnList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // Obtener el índice del AFN seleccionado
                    int index = afnList.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        String afnName = afnListModel.getElementAt(index);
                        // Mostrar el contenido del AFN
                        showAFNContent(afnName);
                    }
                }
            }
        });

        btnConcatenation.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int firstIndex = selectAFN("Seleccione el primer AFN para concatenar:");
                if (firstIndex == -1) return;
                int secondIndex = selectAFN("Seleccione el segundo AFN para concatenar:");
                if (secondIndex == -1) return;

                AFN afn1 = afnStorage.get(firstIndex);
                AFN afn2 = afnStorage.get(secondIndex);

                AFN result = ThompsonAlgorithm.constructForConcatenation(afn1, afn2);
                addAFN(result, "Concat_" + (afnCounter++));
                result.printAFN(); //salida en consola de la construccion en cadena de texto del AFN
                JOptionPane.showMessageDialog(null, "AFNs concatenados correctamente.");
            }
        });

        btnUnion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int firstIndex = selectAFN("Seleccione el primer AFN para unir:");
                if (firstIndex == -1) return;
                int secondIndex = selectAFN("Seleccione el segundo AFN para unir:");
                if (secondIndex == -1) return;

                AFN afn1 = afnStorage.get(firstIndex);
                AFN afn2 = afnStorage.get(secondIndex);

                AFN result = ThompsonAlgorithm.constructForUnion(afn1, afn2);
                addAFN(result, "Union_" + (afnCounter++));
                result.printAFN(); //salida en consola de la construccion en cadena de texto del AFN
                JOptionPane.showMessageDialog(null, "AFNs unidos correctamente.");
            }
        });

        btnKleeneStar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = selectAFN("Seleccione un AFN para aplicar Cerradura de Kleene:");
                if (index == -1) return;

                AFN afn = afnStorage.get(index);
                AFN result = ThompsonAlgorithm.constructForKleeneStar(afn);
                //Actualizacion en almacenamiento
                afnStorage.set(index, result);
                afnListModel.set(index, afnListModel.get(index) + "_Kleene");
                result.printAFN(); //salida en consola de la construccion en cadena de texto del AFN
                JOptionPane.showMessageDialog(null, "Cerradura de Kleene aplicada.");
            }
        });

        btnPositiveClosure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = selectAFN("Seleccione un AFN para aplicar Cerradura Positiva:");
                if (index == -1) return;

                AFN afn = afnStorage.get(index);
                AFN result = ThompsonAlgorithm.constructForPositiveClosure(afn);
                //Actualizacion en almacenamiento
                afnStorage.set(index, result);
                afnListModel.set(index, afnListModel.get(index) + "_Positiva");
                result.printAFN(); //salida en consola de la construccion en cadena de texto del AFN
                JOptionPane.showMessageDialog(null, "Cerradura positiva aplicada.");
            }
        });

        btnOptional.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = selectAFN("Seleccione un AFN para hacerlo Opcional:");
                if (index == -1) return;

                AFN afn = afnStorage.get(index);
                AFN result = ThompsonAlgorithm.constructForOptional(afn);
                //Actualizacion en almacenamiento
                afnStorage.set(index, result);
                afnListModel.set(index, afnListModel.get(index) + "_Opcional");
                result.printAFN(); //salida en consola de la construccion en cadena de texto del AFN
                JOptionPane.showMessageDialog(null, "AFN opcional creado.");
            }
        });

        btnERtoAFN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String regex = JOptionPane.showInputDialog("Ingrese la expresión regular:");
                String tokenType = JOptionPane.showInputDialog("Ingrese el tipo de token:");
                if (regex != null && !regex.isEmpty()) {
                    AFN afn = ThompsonAlgorithm.ERtoAFN(regex, tokenType);
                    addAFN(afn, "ER_" + (afnCounter++));
                    afn.printAFN(); //salida en consola de la construccion en cadena de texto del AFN (ER to AFN)
                    JOptionPane.showMessageDialog(null, "AFN creado a partir de la expresión regular.");
                }
            }
        });
        
        /*
        btnShowSavedAFNContent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String contenido = AFNFile.leerAFNsGuardados();
                if (contenido.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "No hay AFNs guardados.");
                } else {
                    JOptionPane.showMessageDialog(null, contenido, "AFNs Guardados", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });
        */
        
        btnDeleteAFN.addActionListener(new ActionListener() { // <<<<<<
            public void actionPerformed(ActionEvent e) {
                int index = afnList.getSelectedIndex();
                if (index != -1) {
                    int confirm = JOptionPane.showConfirmDialog(null, "¿Seguro que deseas eliminar este AFN?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        afnStorage.remove(index);
                        afnListModel.remove(index);
                        JOptionPane.showMessageDialog(null, "AFN eliminado correctamente.");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Seleccione un AFN para eliminar.");
                }
            }
        });
        
        btnAFNtoAFD.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int index = selectAFN("Seleccione un AFN para convertirlo a AFD:");
                if (index == -1) return;
                AFN selectedAFN = afnStorage.get(index);
                AFD afd = SubsetConstruction.AFNtoAFD(selectedAFN);
                afd.printAFD(); //salida en consola de la construccion en cadena de texto del AFN (ER to AFN)
                JOptionPane.showMessageDialog(null, afd.toString(), "AFD Generado", JOptionPane.INFORMATION_MESSAGE);
                //afdStorage.add(afd);
            }
        });



        btnLexicAnalyzer.addActionListener(new ActionListener() { // <<<<<<
            public void actionPerformed(ActionEvent e) {
                // Aquí llamas a tu otra ventana que ya tienes preparada
                AnalizadorLexicoGUI analizador = new AnalizadorLexicoGUI(); // <<<<<< (tu clase)
                analizador.setVisible(true);
            }
        });

        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    //Métodos
    /*
    private void guardarAFNEnArchivo(AFN afn, String symbol) {
        File afnFile = new File(directoryPath + "/" + symbol + ".txt");
        if (afnFile.exists()) {
            JOptionPane.showMessageDialog(this, "El símbolo '" + symbol + "' ya ha sido utilizado. No se guardará el nuevo AFN.");
            return;
        }
    }
    */
    private void addAFN(AFN afn, String name) {
        afnStorage.add(afn);
        afnListModel.addElement(name);
    }
    
    // Cargar los AFNs guardados desde el directorio
    
    /*
    private void loadSavedAFNs() {
        File dir = new File(directoryPath);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
            if (files != null) {
                for (File file : files) {
                    try {
                        String afnName = file.getName().replace(".txt", "");
                        afnListModel.addElement(afnName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    */
    /*
    private void showAFNContent(String afnName) {
        String content = AFNFile.leerAFNsGuardados(directoryPath, afnName);
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el contenido del AFN.");
        } else {
            JOptionPane.showMessageDialog(this, content, "Contenido de " + afnName, JOptionPane.INFORMATION_MESSAGE);
        }
    }
    */
    private void showAFNContent(String afnName) {
    int index = afnListModel.indexOf(afnName);
    if (index >= 0 && index < afnStorage.size()) {
        AFN afn = afnStorage.get(index);
        String content = afn.toString();
        JOptionPane.showMessageDialog(this, content, "Contenido de " + afnName, JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, "No se encontró el AFN seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
    }
}

    private int selectAFN(String message) {
        Object selected = JOptionPane.showInputDialog(
                this,
                message,
                "Seleccionar AFN",
                JOptionPane.PLAIN_MESSAGE,
                null,
                afnListModel.toArray(),
                null
        );
        if (selected == null) return -1;
        return afnListModel.indexOf(selected.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new menu().setVisible(true);
        });
    }
}
