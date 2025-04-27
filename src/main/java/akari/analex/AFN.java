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
    Set<Integer> acceptingStates;
    private int nextStateId;

    public AFN() {
        this.states = new HashMap<>();
        this.startState = -1;
        this.acceptingStates = new HashSet<>();
        this.nextStateId = 0;
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
    
    //Método para convertir el formato hasheado del objeto afn a una cadena legible
    /*
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AFN con ").append(states.size()).append(" estados.\n");
        sb.append("Estado de inicio: ").append(startState).append("\n");
        sb.append("Estados de aceptación: ").append(acceptingStates).append("\n");
        sb.append("Transiciones:\n");
        
        for (State state : states.values()) {
            sb.append(state).append("\n");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AFN con ").append(states.size()).append(" estados.\n");
        sb.append("Estado de inicio: ").append(startState).append("\n");
        sb.append("Estado de aceptación: ").append(acceptingStates).append("\n");
        sb.append("Transiciones:\n");

        for (State state : states.values()) {
            // Transiciones normales (con símbolo)
            for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
                char symbol = entry.getKey();
                for (Integer destId : entry.getValue()) {
                    sb.append("Estado ").append(state.getId())
                    .append(" --")
                    .append(symbol)
                    .append("--> Estado ")
                    .append(destId)
                    .append("\n");
                }
            }
        
            // Transiciones epsilon (sin símbolo)
            for (Integer destId : state.getEpsilonTransitions()) {
                sb.append("Estado ").append(state.getId())
                .append(" --ε--> Estado ")
                .append(destId)
                .append("\n");
            }
        }

        return sb.toString();
    }
    */
}