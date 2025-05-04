package akari.analex;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Conversión de AFN a AFD utilizando construcción de subconjuntos
 * @author Akari Aguilera
 */
public class SubsetConstruction {

    public static AFD AFNtoAFD(AFN afn) {
    AFD afd = new AFD();

    // Obtener la cerradura épsilon del estado inicial del AFN
    Set<Integer> startStateSet = afn.epsilonClosure(Set.of(afn.getStartState()));
    Queue<Set<Integer>> queue = new LinkedList<>();
    Map<Set<Integer>, Integer> stateMap = new HashMap<>(new HashMapWithSetComparator());

    int nextStateId = 0;
    stateMap.put(startStateSet, nextStateId);
    afd.addState(nextStateId);
    afd.setStartState(nextStateId);

    // Marcar estado de aceptación si algún estado en la cerradura inicial es aceptante
    String tokenType = getAcceptingTokenType(afn, startStateSet);
    
    if (tokenType != null) {
        afd.addAcceptingState(0);
        afd.setTokenType(0, tokenType);
    }


    queue.offer(startStateSet);
    nextStateId++;

    // Obtener el alfabeto del AFN (excluyendo transiciones épsilon)
    Set<Character> alphabet = new HashSet<>();
    for (State state : afn.getStates().values()) {
        alphabet.addAll(state.getTransitions().keySet());
    }
    alphabet.remove('\0'); // Remover épsilon explícito

    // Construcción del AFD mediante subconjuntos
    while (!queue.isEmpty()) {
        Set<Integer> currentSet = queue.poll();
        int fromState = stateMap.get(currentSet);

        for (char symbol : alphabet) {
            Set<Integer> moveSet = afn.transition(currentSet, symbol);
            Set<Integer> epsilonClosureSet = afn.epsilonClosure(moveSet);

            if (epsilonClosureSet.isEmpty()) continue;

            Integer toState = stateMap.get(epsilonClosureSet);
            if (toState == null) {
                toState = nextStateId++;
                stateMap.put(epsilonClosureSet, toState);
                afd.addState(toState);

                // Verificar si algún estado es de aceptación
                String localTokenType = getAcceptingTokenType(afn, epsilonClosureSet);
                if (localTokenType != null) {
                    afd.addAcceptingState(toState);
                    afd.setTokenType(toState, localTokenType);
                }
                queue.offer(epsilonClosureSet);
            }

            afd.addTransition(fromState, symbol, toState);
        }
    }

    return afd;
}

    private static String getAcceptingTokenType(AFN afn, Set<Integer> stateSet) {
        List<State> acceptingStates = stateSet.stream()
                .map(afn::getState)
                .filter(state -> state != null && state.isAccepting() && state.getTokenType() != null)
                .collect(Collectors.toList());

        return acceptingStates.isEmpty() ? null : acceptingStates.get(0).getTokenType();
    }

    /**
     * Comparador personalizado para Map<Set<Integer>, Integer>
     * para asegurar que los conjuntos se comparen por contenido.
     */
    static class HashMapWithSetComparator extends AbstractMap<Set<Integer>, Integer> {
        @Override
        public Set<Entry<Set<Integer>, Integer>> entrySet() {
            return new HashSet<>();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Set)) return false;
            return this.keySet().equals(o);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.keySet());
        }
    }
}
