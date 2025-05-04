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
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 *
 * @author Akari Aguilera
 */

public class AFN {
    private Map<Integer, State> states;
    private int startState;
    protected Set<Integer> acceptingStates;
    protected Map<Integer, String> tokenTypes; // Mapa para los tipos de token
    private int nextStateId;

    public AFN() {
        this.states = new HashMap<>();
        this.startState = -1;
        this.acceptingStates = new HashSet<>();
        this.tokenTypes = new HashMap<>();
        this.nextStateId = 0;
    }
    
    // Método para obtener el tipo de token asociado a un estado de aceptación
    public String getTokenType(int acceptingStateId) {
        return this.tokenTypes.get(acceptingStateId);
    }
    
    // Método para asociar un tipo de token a un estado de aceptación
    public void setTokenType(int acceptingStateId, String tokenType) {
        if (this.acceptingStates.contains(acceptingStateId)) {
            this.tokenTypes.put(acceptingStateId, tokenType);
        } else {
            System.out.println("Estado no es de aceptación: " + acceptingStateId);
        }
    }

    public State createState() {
        State newState = new State(nextStateId++);
        this.states.put(newState.getId(), newState);
        return newState;
    }

    public State getState(int id) {
        return this.states.get(id);
    }

    public Map<Integer, State> getStates() {
        return this.states;
    }

    public int getStartState() {
        return startState;
    }

    public void setStartState(int startState) {
        this.startState = startState;
    }

    public Set<Integer> getAcceptingStates() {
        return acceptingStates;
    }

    public void addAcceptingState(int stateId) {
        this.acceptingStates.add(stateId);
        if (this.states.containsKey(stateId)) {
            this.states.get(stateId).setAccepting(true);
        }
    }

    public void addTransition(int fromState, char symbol, int toState) {
        if (this.states.containsKey(fromState)) {
            this.states.get(fromState).addTransition(symbol, toState);
        }
    }

    public void addEpsilonTransition(int fromState, int toState) {
        if (this.states.containsKey(fromState)) {
            this.states.get(fromState).addEpsilonTransition(toState);
        }
    }

    // Método para obtener los estados alcanzables con transiciones épsilon
    public Set<Integer> epsilonClosure(Set<Integer> states) {
        Set<Integer> closure = new HashSet<>(states);
        Stack<Integer> stack = new Stack<>();
        stack.addAll(states);

        while (!stack.isEmpty()) {
            int currentStateId = stack.pop();
            State currentState = this.states.get(currentStateId);
            if (currentState != null) {
                for (int nextStateId : currentState.getEpsilonTransitions()) {
                    if (!closure.contains(nextStateId)) {
                        closure.add(nextStateId);
                        stack.push(nextStateId);
                    }
                }
            }
        }
        return closure;
    }

    // Método para obtener los estados alcanzables con un símbolo desde un conjunto de estados
    public Set<Integer> transition(Set<Integer> states, char symbol) {
        Set<Integer> nextStates = new HashSet<>();
        for (int stateId : states) {
            State state = this.states.get(stateId);
            if (state != null && state.getTransitions().containsKey(symbol)) {
                nextStates.addAll(state.getTransitions().get(symbol));
            }
        }
        return nextStates;
    }

    public void printAFN() {
        System.out.println("--- AFN ---");
        System.out.println("Estados: " + this.states.keySet());
        System.out.println("Estado inicial: " + this.startState);
        System.out.println("Estados de aceptación: " + this.acceptingStates);
        // Mostrar tokens asociados a los estados de aceptación
        System.out.println("Tokens asociados al estado de aceptación:");
        for (int acceptingState : this.acceptingStates) {
            String tokenType = this.getState(acceptingState).getTokenType();
            System.out.println("  Estado " + acceptingState + " => Token: " + (tokenType != null ? tokenType : "Ninguno"));
        }
        System.out.println("Transiciones:");
        for (Map.Entry<Integer, State> entry : this.states.entrySet()) {
            int stateId = entry.getKey();
            State state = entry.getValue();
            for (Map.Entry<Character, Set<Integer>> transitionEntry : state.getTransitions().entrySet()) {
                char symbol = transitionEntry.getKey();
                Set<Integer> nextStates = transitionEntry.getValue();
                System.out.println("  Estado " + stateId + " -- '" + symbol + "' --> " + nextStates);
            }
            if (!state.getEpsilonTransitions().isEmpty()) {
                System.out.println("  Estado " + stateId + " -- ε --> " + state.getEpsilonTransitions());
            }
        }
        System.out.println("--- Fin AFN ---");
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
    
        sb.append("--- AFN ---\n");
        sb.append("Estados: ").append(this.states.keySet()).append("\n");
        sb.append("Estado de inicio: ").append(this.startState).append("\n");
        sb.append("Estados de aceptación: ").append(this.acceptingStates).append("\n");
        sb.append("Tipos de token asociados al estado de aceptación:\n");
        for (int acceptingState : this.acceptingStates) {
            String tokenType = this.getState(acceptingState).getTokenType();
            sb.append("  Estado ").append(acceptingState)
            .append(" => Token: ").append(tokenType != null ? tokenType : "Ninguno").append("\n");
        }
        
        sb.append("Transiciones:\n");
        // Transiciones normales (con símbolo)
        for (Map.Entry<Integer, State> entry : this.states.entrySet()) {
            int stateId = entry.getKey();
            State state = entry.getValue();
            for (Map.Entry<Character, Set<Integer>> transitionEntry : state.getTransitions().entrySet()) {
                char symbol = transitionEntry.getKey();
                Set<Integer> nextStates = transitionEntry.getValue();
                sb.append("  Estado ").append(stateId).append(" -- '").append(symbol).append("' --> ").append(nextStates).append("\n");
            }
            
            if (!state.getEpsilonTransitions().isEmpty()) {
                sb.append("  Estado ").append(stateId).append(" -- ε --> ").append(state.getEpsilonTransitions()).append("\n");
            }
        }
        sb.append("--- Fin AFN ---\n");

        return sb.toString();
    }
}