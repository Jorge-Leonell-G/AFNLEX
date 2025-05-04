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

/**
 *
 * @author Akari Aguilera
 */
public class State {
    private int id;
    private Map<Character, Set<Integer>> transitions;
    private Set<Integer> epsilonTransitions;
    private boolean isAccepting;
    private String tokenType; // Nuevo atributo para el tipo de token

    public State(int id) {
        this.id = id;
        this.transitions = new HashMap<>();
        this.epsilonTransitions = new HashSet<>();
        this.isAccepting = false;
        this.tokenType = null; // Inicialmente no tiene tipo
    }

    public int getId() {
        return id;
    }

    public Map<Character, Set<Integer>> getTransitions() {
        return transitions;
    }

    public Set<Integer> getEpsilonTransitions() {
        return epsilonTransitions;
    }

    public void addTransition(char symbol, int toState) {
        transitions.computeIfAbsent(symbol, k -> new HashSet<>()).add(toState);
    }

    public void addEpsilonTransition(int toState) {
        epsilonTransitions.add(toState);
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void setAccepting(boolean accepting) {
        isAccepting = accepting;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return id == state.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
    
    @Override
    public String toString() {
        return "Estado " + id + (isAccepting ? " (de aceptación, token: )" + tokenType + ")" : "");
    }
}