/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akari.analex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author Akari Aguilera
 */
// akari/analex/AFD.java
public class AFD {
    private Map<Integer, Map<Character, Integer>> transitions;
    private Set<Integer> acceptingStates;
    private int startState;
    private Set<Integer> states;
    private Map<Integer, String> acceptingTokenTypes; // Mapa para almacenar el tipo de token de los estados de aceptación

    public AFD() {
        this.transitions = new HashMap<>();
        this.acceptingStates = new HashSet<>();
        this.startState = -1;
        this.states = new HashSet<>();
        this.acceptingTokenTypes = new HashMap<>();
    }

    public void addState(int stateId) {
        this.states.add(stateId);
    }

    public void addTransition(int fromState, char symbol, int toState) {
        this.transitions.computeIfAbsent(fromState, k -> new HashMap<>()).put(symbol, toState);
    }

    public void setStartState(int startState) {
        this.startState = startState;
    }

    public void addAcceptingState(int stateId) {
        this.acceptingStates.add(stateId);
    }

    public void setTokenType(int stateId, String tokenType) {
        if (this.acceptingStates.contains(stateId)) {
            this.acceptingTokenTypes.put(stateId, tokenType);
        }
    }

    public String getTokenType(int stateId) {
        return this.acceptingTokenTypes.get(stateId);
    }

    public Map<Character, Integer> getTransitions(int state) {
        return this.transitions.get(state);
    }

    public Set<Integer> getAcceptingStates() {
        return this.acceptingStates;
    }

    public int getStartState() {
        return this.startState;
    }

    public Set<Integer> getStates() {
        return this.states;
    }

    public boolean isAccepting(int state) {
        return this.acceptingStates.contains(state);
    }

    public int getNextState(int currentState, char symbol) {
        Map<Character, Integer> currentTransitions = this.transitions.get(currentState);
        if (currentTransitions != null && currentTransitions.containsKey(symbol)) {
            return currentTransitions.get(symbol);
        }
        return -1; // No hay transición definida para este símbolo desde el estado actual
    }

    public List<Token> analyze(String input) {
        List<Token> tokens = new ArrayList<>();
        int currentState = this.startState;
        StringBuilder currentLexeme = new StringBuilder();
        int linea = 1;
        int columna = 1;
        int tokenStartColumn = 1;
        String matchedTokenType = null;
        int lastAcceptingState = -1;
        int lastAcceptingIndex = -1;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);
            int nextState = getNextState(currentState, currentChar);

            if (nextState != -1) {
                currentState = nextState;
                currentLexeme.append(currentChar);
                if (isAccepting(currentState)) {
                    lastAcceptingState = currentState;
                    lastAcceptingIndex = i;
                }
            } else {
                if (lastAcceptingState != -1) {
                    String tokenValue = input.substring(tokenStartColumn - 1, lastAcceptingIndex + 1);
                    String tokenType = getTokenType(lastAcceptingState);
                    if (tokenType != null) {
                        tokens.add(new Token(tokenType, tokenValue, linea, tokenStartColumn));
                        currentLexeme = new StringBuilder();
                        currentState = this.startState;
                        tokenStartColumn = i + 1;
                        lastAcceptingState = -1;
                        lastAcceptingIndex = -1;
                        // Re-procesar el carácter actual
                        i--;
                    } else {
                        System.err.println("Error léxico (AFD) en línea " + linea + ", columna " + tokenStartColumn + ": Se reconoció '" + tokenValue + "' pero no tiene tipo.");
                        currentLexeme = new StringBuilder();
                        currentState = this.startState;
                        tokenStartColumn = i + 1;
                        lastAcceptingState = -1;
                        lastAcceptingIndex = -1;
                    }
                } else {
                    System.err.println("Error léxico (AFD) en línea " + linea + ", columna " + columna + ": Carácter inesperado '" + currentChar + "'.");
                    currentLexeme = new StringBuilder();
                    currentState = this.startState;
                    tokenStartColumn = i + 2; // Avanzar después del error
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

        // Procesar el último lexema si se alcanzó un estado de aceptación
        if (lastAcceptingState != -1 && currentLexeme.length() > 0) {
            String tokenValue = input.substring(tokenStartColumn - 1, input.length());
            String tokenType = getTokenType(lastAcceptingState);
            if (tokenType != null) {
                tokens.add(new Token(tokenType, tokenValue, linea, tokenStartColumn));
            } else {
                System.err.println("Error léxico (AFD): Fin de entrada inesperado para '" + tokenValue + "' sin tipo.");
            }
        } else if (currentLexeme.length() > 0 && lastAcceptingState == -1) {
            System.err.println("Error léxico (AFD): Fin de entrada inesperado para '" + currentLexeme + "'.");
        }

        return tokens;
    }

    public void printAFD() {
        System.out.println("--- AFD ---");
        System.out.println("Estados: " + this.states);
        System.out.println("Estado inicial: " + this.startState);
        System.out.println("Estados de aceptación: " + this.acceptingStates);
        System.out.println("Tipos de token de aceptación: " + this.acceptingTokenTypes);
        System.out.println("Transiciones:");
        for (int fromState : this.states) {
            Map<Character, Integer> stateTransitions = this.transitions.get(fromState);
            if (stateTransitions != null) {
                for (Map.Entry<Character, Integer> entry : stateTransitions.entrySet()) {
                    char symbol = entry.getKey();
                    int toState = entry.getValue();
                    System.out.println("  Estado " + fromState + " -- '" + symbol + "' --> " + toState);
                }
            }
        }
        System.out.println("--- Fin AFD ---");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    
        sb.append("--- AFD ---\n");
        sb.append("Estados: ").append(this.states).append("\n");
        sb.append("Estado de inicio: ").append(this.startState).append("\n");
        sb.append("Estados de aceptación: ").append(this.acceptingStates).append("\n");
        sb.append("Tipos de token de aceptación: ").append(this.acceptingTokenTypes).append("\n");
        sb.append("Transiciones:\n");

        // Transiciones normales (con símbolo)
        for (int fromState : this.states) {
            Map<Character, Integer> stateTransitions = this.transitions.get(fromState);
            if (stateTransitions != null) {
                for (Map.Entry<Character, Integer> entry : stateTransitions.entrySet()) {
                    char symbol = entry.getKey();
                    int toState = entry.getValue();
                    sb.append("  Estado ").append(fromState).append(" -- '").append(symbol).append("' --> ").append(toState).append("\n");
                }
            }
        }
        sb.append("--- Fin AFD ---\n");

        return sb.toString();
}
}