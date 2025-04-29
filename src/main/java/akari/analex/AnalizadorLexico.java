// akari/analex/AnalizadorLexico.java
package akari.analex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnalizadorLexico {

    private List<Token> tokens;
    AFD analizadorAFD;
    private List<ReglaLexica> reglasLexicas;

    public AnalizadorLexico() {
        this.tokens = new ArrayList<>();
        this.reglasLexicas = new ArrayList<>();
    }

    public AFD getAnalizadorAFD() {
        return this.analizadorAFD;
    }

    private static class ReglaLexica {
        String tipo;
        String patron;

        public ReglaLexica(String tipo, String patron) {
            this.tipo = tipo;
            this.patron = patron;
        }
    }

    public void agregarReglaLexica(String tipo, String patron) {
        this.reglasLexicas.add(new ReglaLexica(tipo, patron));
    }

    public List<ReglaLexica> obtenerReglasLexicas() {
        return this.reglasLexicas;
    }

    // ------------------- Métodos del Menú -------------------

    public List<Token> analisisBasico(String input) {
        System.out.println("Realizando análisis básico (usando reglas directas): " + input);
        return this.analizarCadenaConRegex(input);
    }

    public String unir(String cadena1, String cadena2) {
        System.out.println("Uniendo cadenas: " + cadena1 + " + " + cadena2);
        return cadena1 + cadena2;
    }

    public String concatenar(String cadena1, String cadena2) {
        System.out.println("Concatenando cadenas: " + cadena1 + " + " + cadena2);
        return cadena1 + cadena2;
    }

    public String cerraduraPositiva(String regex) {
        System.out.println("Aplicando cerradura + a: " + regex);
        return "(" + regex + ")+";
    }

    public String cerraduraKleene(String regex) {
        System.out.println("Aplicando cerradura * a: " + regex);
        return "(" + regex + ")*";
    }

    public String opcional(String regex) {
        System.out.println("Haciendo opcional: " + regex);
        return "(" + regex + ")?";
    }

    public AFN convertirERaAFN(String regex) {
        System.out.println("Convirtiendo ER a AFN: " + regex);
        return ThompsonAlgorithm.ERtoAFN(regex, null);
    }

    public AFN convertirERaAFNConTipo(String regex, String tipoToken) {
        System.out.println("Convirtiendo ER a AFN con tipo: " + regex + " -> " + tipoToken);
        return ThompsonAlgorithm.ERtoAFN(regex, tipoToken);
    }

    public void unionAnalizadorLexico(List<ReglaLexica> reglas) {
        System.out.println("Realizando unión de expresiones regulares para el analizador léxico.");
        if (reglas.isEmpty()) {
            return;
        }

        AFN combinedAFN = new AFN();
        State startState = combinedAFN.createState();
        combinedAFN.setStartState(startState.getId());
        State finalAcceptState = combinedAFN.createState(); 

        for (ReglaLexica regla : reglas) {
            AFN afn = ThompsonAlgorithm.ERtoAFN(regla.patron, regla.tipo);
            if (!afn.getStates().isEmpty()) {
                State ruleStartState = combinedAFN.createState();
                combinedAFN.addEpsilonTransition(startState.getId(), ruleStartState.getId());

                Map<Integer, Integer> stateMap = new HashMap<>();
                for (State state : afn.getStates().values()) {
                    int newStateId = combinedAFN.createState().getId();
                    stateMap.put(state.getId(), newStateId);

                    if (afn.getStartState() == state.getId()) {
                        combinedAFN.addEpsilonTransition(ruleStartState.getId(), newStateId);
                    }
                    if (afn.getAcceptingStates().contains(state.getId())) {
                        combinedAFN.addEpsilonTransition(newStateId, finalAcceptState.getId());
                        State newState = combinedAFN.getState(newStateId);
                        if (newState != null) {
                            newState.setAccepting(true);
                            newState.setTokenType(regla.tipo);
                        }
                    }
                }

                for (State state : afn.getStates().values()) {
                    int fromStateId = stateMap.get(state.getId());
                    for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
                        char symbol = entry.getKey();
                        for (int toStateId : entry.getValue()) {
                            combinedAFN.addTransition(fromStateId, symbol, stateMap.get(toStateId));
                        }
                    }
                    for (int toStateId : state.getEpsilonTransitions()) {
                        combinedAFN.addEpsilonTransition(fromStateId, stateMap.get(toStateId));
                    }
                }
            }
        }

        if (!combinedAFN.getStates().isEmpty()) {
            combinedAFN.addAcceptingState(finalAcceptState.getId());
            this.analizadorAFD = convertirAFNaAFD(combinedAFN);
        } else {
            System.err.println("No se pudieron combinar las expresiones regulares.");
            this.analizadorAFD = null;
        }
    }

    public AFD convertirAFNaAFD(AFN afn) {
        System.out.println("Convirtiendo AFN a AFD.");
        return SubsetConstruction.AFNtoAFD(afn);
    }

    public void cargarAFD(AFD afd) {
        System.out.println("Cargando AFD para el análisis.");
        this.analizadorAFD = afd;
    }

    public List<Token> analizarCadena(String input) {
        System.out.println("Analizando cadena con AFD: " + input);
        if (this.analizadorAFD != null) {
            return this.analizarCadenaConAFD(input);
        } else {
            System.err.println("Error: No se ha cargado un AFD para el análisis.");
            return new ArrayList<>();
        }
    }

    private List<Token> analizarCadenaConAFD(String input) {
        List<Token> tokens = new ArrayList<>();
        int currentState = analizadorAFD.getStartState();
        StringBuilder currentLexeme = new StringBuilder();
        int linea = 1;
        int columna = 1;
        int tokenStartColumn = 1;
        String matchedTokenType = null;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            int nextState = analizadorAFD.getNextState(currentState, currentChar);

            if (nextState != -1) {
                currentState = nextState;
                currentLexeme.append(currentChar);
                if (analizadorAFD.isAccepting(currentState)) {
                    matchedTokenType = analizadorAFD.getTokenType(currentState);
                }
            } else {
                if (currentLexeme.length() > 0 && matchedTokenType != null) {
                    tokens.add(new Token(matchedTokenType, currentLexeme.toString(), linea, tokenStartColumn));
                    currentLexeme = new StringBuilder();
                    currentState = analizadorAFD.getStartState();
                    tokenStartColumn = columna;
                    matchedTokenType = null;
                    nextState = analizadorAFD.getNextState(currentState, currentChar);
                    if (nextState != -1) {
                        currentState = nextState;
                        currentLexeme.append(currentChar);
                        if (analizadorAFD.isAccepting(currentState)) {
                            matchedTokenType = analizadorAFD.getTokenType(currentState);
                        }
                    } else {
                        System.err.println("Error léxico (AFD) en línea " + linea + ", columna " + columna + ": Carácter inesperado '" + currentChar + "'.");
                        currentState = analizadorAFD.getStartState();
                        tokenStartColumn = columna + 1;
                    }
                } else {
                    if (currentLexeme.length() > 0) {
                        System.err.println("Error léxico (AFD) en línea " + linea + ", columna " + tokenStartColumn + ": No se reconoce '" + currentLexeme + "'.");
                    } else {
                        System.err.println("Error léxico (AFD) en línea " + linea + ", columna " + columna + ": Carácter no reconocido '" + currentChar + "'.");
                    }
                    currentLexeme = new StringBuilder();
                    currentState = analizadorAFD.getStartState();
                    tokenStartColumn = columna + 1;
                    matchedTokenType = null;
                }
            }

            if (currentChar == '\n') {
                linea++;
                columna = 1;
                tokenStartColumn = 1;
            } else {
                columna++;
            }
        }

        if (currentLexeme.length() > 0 && matchedTokenType != null) {
            tokens.add(new Token(matchedTokenType, currentLexeme.toString(), linea, tokenStartColumn));
        } else if (currentLexeme.length() > 0) {
            System.err.println("Error léxico (AFD): Fin de entrada inesperado para '" + currentLexeme + "'.");
        }

        return tokens;
    }

    private List<Token> analizarCadenaConRegex(String input) {
        List<Token> tokens = new ArrayList<>();
        while (!input.isEmpty()) {
            boolean matched = false;
            for (ReglaLexica regla : this.reglasLexicas) {
                Pattern pattern = Pattern.compile("^" + regla.patron);
                Matcher matcher = pattern.matcher(input);
                if (matcher.find()) {
                    String lexema = matcher.group();
                    tokens.add(new Token(regla.tipo, lexema, 1, 1));
                    input = input.substring(lexema.length());
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                System.err.println("Error léxico (regex): No se reconoce el inicio de: " + input);
                break;
            }
        }
        return tokens;
    }

    public void probarAnalizadorLexico(String input, List<String> reglas) {
        List<Token> tokensEncontrados = analizarCadenaConRegex(input);
        if (tokensEncontrados != null) {
            for (Token token : tokensEncontrados) {
                System.out.println(token);
            }
        }
    }

    public static void main(String[] args) {
        AnalizadorLexico analizador = new AnalizadorLexico();

        analizador.agregarReglaLexica("PALABRA_CLAVE", "if|else");
        analizador.agregarReglaLexica("IDENTIFICADOR", "[a-zA-Z][a-zA-Z0-9]*");
        analizador.agregarReglaLexica("NUMERO", "[0-9]+");
        analizador.agregarReglaLexica("SIMBOLO", "\\+|\\-|\\*|/\\@\\(\\)\\->");
        analizador.agregarReglaLexica("ESPACIO", "\\s+");

        analizador.unionAnalizadorLexico(analizador.obtenerReglasLexicas());

        String codigoFuente = "if contador123 else 42 + otroId - 5";
        List<Token> resultadoAFD = analizador.analizarCadena(codigoFuente);
        System.out.println("\nTokens encontrados (usando AFD):");
        for (Token token : resultadoAFD) {
            System.out.println(token);
        }

        System.out.println("\nTokens encontrados (usando reglas directas):");
        analizador.probarAnalizadorLexico(codigoFuente, null);
    }
}
