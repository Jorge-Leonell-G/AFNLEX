package akari.analex;

import java.util.List;

public class AnaLex {
    public static void main(String[] args) {
        System.out.println("¡Ejecutando AnaLex!");

        // Crear una instancia de tu analizador léxico
        AnalizadorLexico analizador = new AnalizadorLexico();

        // Definir algunas reglas léxicas (puedes mover esto a otro lugar si lo prefieres)
        analizador.agregarReglaLexica("PALABRA_CLAVE", "if|else");
        analizador.agregarReglaLexica("IDENTIFICADOR", "[a-zA-Z][a-zA-Z0-9]*");
        analizador.agregarReglaLexica("NUMERO", "[0-9]+");
        analizador.agregarReglaLexica("OPERADOR", "\\+|\\-|\\*|/");
        analizador.agregarReglaLexica("ESPACIO", "\\s+"); // Importante para ignorar espacios

        // Construir el AFD a partir de las reglas
        analizador.unionAnalizadorLexico(analizador.obtenerReglasLexicas());

        // Cadena de entrada para probar el analizador
        String codigoFuente = "if contador123 else 42 + otroId - 5";

        // Analizar la cadena de entrada
        List<Token> resultadoAFD = analizador.analizarCadena(codigoFuente);

        // Imprimir los tokens encontrados
        System.out.println("\nTokens encontrados (usando AFD):");
        for (Token token : resultadoAFD) {
            System.out.println(token);
        }

        // Puedes agregar más lógica aquí, como interactuar con la GUI
        // o leer la entrada desde un archivo.
    }
}