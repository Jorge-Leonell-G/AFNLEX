/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akari.analex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
/**
 *
 * @author Akari Aguilera
 */

public class SubsetConstruction {
    public static AFD AFNtoAFD(AFN afn) {
        AFD afd = new AFD();
        Set<Integer> startState = afn.epsilonClosure(Set.of(afn.getStartState()));
        Queue<Set<Integer>> queue = new LinkedList<>();
        queue.offer(startState);
        Map<Set<Integer>, Integer> stateMap = new HashMap<>();
        stateMap.put(startState, afd.getStartState());
        afd.addState(afd.getStartState());
        String startStateType = getAcceptingTokenType(afn, startState);
        if (startStateType != null) {
            afd.addAcceptingState(afd.getStartState());
            afd.setTokenType(afd.getStartState(), startStateType);
        }
        int nextStateId = 1;

        while (!queue.isEmpty()) {
            Set<Integer> currentStateSet = queue.poll();
            int fromAFDState = stateMap.get(currentStateSet);

            Set<Character> alphabet = new HashSet<>();
            for (State state : afn.getStates().values()) {
                alphabet.addAll(state.getTransitions().keySet());
            }
            alphabet.remove('\0'); // Remover el carácter nulo si existe

            for (char symbol : alphabet) {
                Set<Integer> nextStateSet = afn.epsilonClosure(afn.transition(currentStateSet, symbol));
                if (!nextStateSet.isEmpty()) {
                    if (!stateMap.containsKey(nextStateSet)) {
                        stateMap.put(nextStateSet, nextStateId);
                        afd.addState(nextStateId);
                        String acceptingType = getAcceptingTokenType(afn, nextStateSet);
                        if (acceptingType != null) {
                            afd.addAcceptingState(nextStateId);
                            afd.setTokenType(nextStateId, acceptingType);
                        }
                        queue.offer(nextStateSet);
                        afd.addTransition(fromAFDState, symbol, nextStateId++);
                    } else {
                        afd.addTransition(fromAFDState, symbol, stateMap.get(nextStateSet));
                    }
                }
            }
        }
        return afd;
    }

    private static String getAcceptingTokenType(AFN afn, Set<Integer> stateSet) {
        List<State> acceptingStates = stateSet.stream()
                .map(afn::getState)
                .filter(state -> state != null && state.isAccepting() && state.getTokenType() != null)
                .collect(Collectors.toList());

        if (!acceptingStates.isEmpty()) {
            // Aquí podríamos implementar la lógica de prioridad si es necesario.
            // Por ahora, simplemente retornamos el tipo del primer estado de aceptación que encontramos.
            return acceptingStates.get(0).getTokenType();
        }
        return null;
    }
}