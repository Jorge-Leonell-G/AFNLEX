package akari.analex;
import akari.analex.AFN;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import javax.swing.JOptionPane;

public class AFNFile{

    private static final String FILE_PATH = "afns_guardados.txt";

    public static void guardarAFN(AFN content, String symbol, String directory) {
        File afnFile = new File(directory + "/" + symbol + ".txt");
        if (afnFile.exists()) {
            JOptionPane.showMessageDialog(null, "El símbolo '" + symbol + "' ya ha sido utilizado. No se guardará el nuevo AFN.");
            return;
        }
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(afnFile)); // true = append
            writer.write(content.toString() + "\n");
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar el archivo: " + e.getMessage());
        }
        
        //Almacenamiento directo dentro del fichero para el listado FILE_PATH
        try (FileWriter writer = new FileWriter(FILE_PATH, true)) { // true = append
            writer.write(content.toString() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String leerAFNsGuardados(String directory, String afn) {
        StringBuilder contenido = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                contenido.append(linea).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contenido.toString();
    }
}
