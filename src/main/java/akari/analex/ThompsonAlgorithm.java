/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package akari.analex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
/**
 *
 * @author Akari Aguilera
 */

public class ThompsonAlgorithm {

    static AFN constructForSymbol(AFN afn, char symbol, String tokenType) {
        State startState = afn.createState();
        State acceptState = afn.createState();
        afn.addTransition(startState.getId(), symbol, acceptState.getId());
        afn.setStartState(startState.getId());
        afn.addAcceptingState(acceptState.getId());
        if (tokenType != null) {
            //afn.getState(acceptState.getId()).setTokenType(tokenType);
            State state = afn.getState(acceptState.getId());
            state.setTokenType(tokenType);
            afn.tokenTypes.put(acceptState.getId(), tokenType);
        }
        return afn;
    }

    static AFN constructForConcatenation(AFN afn1, AFN afn2) {
    AFN resultAFN = new AFN();

    // Mapear y copiar estados de afn1
    Map<Integer, Integer> afn1StateMap = new HashMap<>();
    for (State state : afn1.getStates().values()) {
        State newState = resultAFN.createState();
        afn1StateMap.put(state.getId(), newState.getId());
        if (afn1.getStartState() == state.getId()) {
            resultAFN.setStartState(newState.getId());
        }
        if (afn1.getAcceptingStates().contains(state.getId())) {
            // Agregaremos transiciones epsilon luego, no conservar como aceptante aquí
            // pero marcamos como aceptante temporalmente para identificarlo después
            resultAFN.addAcceptingState(newState.getId());
        }
    }

    // Copiar transiciones de afn1
    for (State state : afn1.getStates().values()) {
        int from = afn1StateMap.get(state.getId());
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            for (int to : entry.getValue()) {
                resultAFN.addTransition(from, entry.getKey(), afn1StateMap.get(to));
            }
        }
        for (int to : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(from, afn1StateMap.get(to));
        }
    }

    // Mapear y copiar estados de afn2
    Map<Integer, Integer> afn2StateMap = new HashMap<>();
    for (State state : afn2.getStates().values()) {
        State newState = resultAFN.createState();
        afn2StateMap.put(state.getId(), newState.getId());
    }

    // Copiar transiciones de afn2
    for (State state : afn2.getStates().values()) {
        int from = afn2StateMap.get(state.getId());
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            for (int to : entry.getValue()) {
                resultAFN.addTransition(from, entry.getKey(), afn2StateMap.get(to));
            }
        }
        for (int to : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(from, afn2StateMap.get(to));
        }
    }

    // Conectar los estados de aceptación de afn1 con el estado inicial de afn2
    int afn2Start = afn2StateMap.get(afn2.getStartState());
    for (int oldAccepting : new HashSet<>(resultAFN.getAcceptingStates())) {
        resultAFN.getState(oldAccepting).setAccepting(false);
        resultAFN.addEpsilonTransition(oldAccepting, afn2Start);
    }

    // Establecer nuevos estados de aceptación (desde afn2)
    Set<Integer> newAcceptingStates = new HashSet<>();
    for (int originalAcceptId : afn2.getAcceptingStates()) {
        int mappedId = afn2StateMap.get(originalAcceptId);
        resultAFN.addAcceptingState(mappedId);

        // Copiar tokenType
        String tokenType = afn2.getState(originalAcceptId).getTokenType();
        if (tokenType != null) {
            resultAFN.getState(mappedId).setTokenType(tokenType);
        }
        newAcceptingStates.add(mappedId);
    }

    resultAFN.acceptingStates = newAcceptingStates;

    System.out.println("--- Debug: Tokens en estados de aceptación ---");
    for (int id : resultAFN.getAcceptingStates()) {
        System.out.println("Estado " + id + " => Token: " + resultAFN.getState(id).getTokenType());
    }

    return resultAFN;
}


    static AFN constructForUnion(AFN afn1, AFN afn2) {
        AFN resultAFN = new AFN();
        State startState = resultAFN.createState();
        resultAFN.setStartState(startState.getId());

        // Mapear y copiar estados de afn1
        Map<Integer, Integer> afn1StateMap = new HashMap<>();
        for (State state : afn1.getStates().values()) {
            int newId = resultAFN.createState().getId();
            afn1StateMap.put(state.getId(), newId);
            if (afn1.getStartState() == state.getId()) {
                resultAFN.addEpsilonTransition(startState.getId(), newId);
            }
        }
        for (State state : afn1.getStates().values()) {
            int fromStateId = afn1StateMap.get(state.getId());
            for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
                char symbol = entry.getKey();
                for (int toStateId : entry.getValue()) {
                    resultAFN.addTransition(fromStateId, symbol, afn1StateMap.get(toStateId));
                }
            }
            for (int toStateId : state.getEpsilonTransitions()) {
                resultAFN.addEpsilonTransition(fromStateId, afn1StateMap.get(toStateId));
            }
        }

        // Mapear y copiar estados de afn2
        Map<Integer, Integer> afn2StateMap = new HashMap<>();
        for (State state : afn2.getStates().values()) {
            int newId = resultAFN.createState().getId();
            afn2StateMap.put(state.getId(), newId);
            if (afn2.getStartState() == state.getId()) {
                resultAFN.addEpsilonTransition(startState.getId(), newId);
            }
        }
        for (State state : afn2.getStates().values()) {
            int fromStateId = afn2StateMap.get(state.getId());
            for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
                char symbol = entry.getKey();
                for (int toStateId : entry.getValue()) {
                    resultAFN.addTransition(fromStateId, symbol, afn2StateMap.get(toStateId));
                }
            }
            for (int toStateId : state.getEpsilonTransitions()) {
                resultAFN.addEpsilonTransition(fromStateId, afn2StateMap.get(toStateId));
            }
        }

        // Marcar estados de aceptación y conservar tipo de token
        for (int oldAcceptId : afn1.getAcceptingStates()) {
            int newAcceptId = afn1StateMap.get(oldAcceptId);
            resultAFN.addAcceptingState(newAcceptId);
            String tokenType = afn1.getState(oldAcceptId).getTokenType();
            if (tokenType != null) {
                resultAFN.getState(newAcceptId).setTokenType(tokenType);
            }
        }

        for (int oldAcceptId : afn2.getAcceptingStates()) {
            int newAcceptId = afn2StateMap.get(oldAcceptId);
            resultAFN.addAcceptingState(newAcceptId);
            String tokenType = afn2.getState(oldAcceptId).getTokenType();
            if (tokenType != null) {
                resultAFN.getState(newAcceptId).setTokenType(tokenType);
            }
        }

        return resultAFN;
    }


   static AFN constructForKleeneStar(AFN afn) {
    AFN resultAFN = new AFN();
    State startState = resultAFN.createState();
    State acceptState = resultAFN.createState();
    resultAFN.setStartState(startState.getId());
    resultAFN.addAcceptingState(acceptState.getId());

    // Asignar el tokenType del primer estado de aceptación del AFN original
    for (int acceptId : afn.getAcceptingStates()) {
        State original = afn.getState(acceptId);
        if (original.getTokenType() != null) {
            resultAFN.getState(acceptState.getId()).setTokenType(original.getTokenType());
            break;
        }
    }

    Map<Integer, Integer> stateMap = new HashMap<>();
    for (State state : afn.getStates().values()) {
        int newStateId = resultAFN.createState().getId();
        stateMap.put(state.getId(), newStateId);

        // Conectar el nuevo estado de inicio con el antiguo
        if (afn.getStartState() == state.getId()) {
            resultAFN.addEpsilonTransition(startState.getId(), newStateId);
        }

        // Conectar antiguos estados de aceptación al nuevo de aceptación y a inicio
        if (afn.getAcceptingStates().contains(state.getId())) {
            resultAFN.addEpsilonTransition(newStateId, acceptState.getId());
            resultAFN.addEpsilonTransition(newStateId, stateMap.get(afn.getStartState()));
        }
    }

    // Copiar transiciones
    for (State state : afn.getStates().values()) {
        int fromStateId = stateMap.get(state.getId());
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            char symbol = entry.getKey();
            for (int toStateId : entry.getValue()) {
                resultAFN.addTransition(fromStateId, symbol, stateMap.get(toStateId));
            }
        }
        for (int toStateId : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(fromStateId, stateMap.get(toStateId));
        }
    }

    // Conectar directamente el nuevo inicio con el nuevo final (para la cadena vacía)
    resultAFN.addEpsilonTransition(startState.getId(), acceptState.getId());

    // Debug opcional
    System.out.println("--- Debug: Tokens en estados de aceptación (Kleene Star) ---");
    for (int id : resultAFN.getAcceptingStates()) {
        System.out.println("Estado " + id + " => Token: " + resultAFN.getState(id).getTokenType());
    }

    return resultAFN;
}


    static AFN constructForPositiveClosure(AFN afn) {
        AFN afnStar = constructForKleeneStar(afn);
        return constructForConcatenation(afn, afnStar);
    }

    static AFN constructForOptional(AFN afn) {
        AFN resultAFN = new AFN();
        State startState = resultAFN.createState();
        State acceptState = resultAFN.createState();

        resultAFN.setStartState(startState.getId());
        resultAFN.addAcceptingState(acceptState.getId());

        Map<Integer, Integer> stateMap = new HashMap<>();
        for (State state : afn.getStates().values()) {
            stateMap.put(state.getId(), resultAFN.createState().getId());

            if (afn.getStartState() == state.getId()) {
                resultAFN.addEpsilonTransition(startState.getId(), stateMap.get(state.getId()));
            }
            if (afn.getAcceptingStates().contains(state.getId())) {
                int mappedAcceptStateId = stateMap.get(state.getId());
                resultAFN.addEpsilonTransition(mappedAcceptStateId, acceptState.getId());
            }
        }

        for (State state : afn.getStates().values()) {
            int fromStateId = stateMap.get(state.getId());
            for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
                char symbol = entry.getKey();
                for (int toStateId : entry.getValue()) {
                    resultAFN.addTransition(fromStateId, symbol, stateMap.get(toStateId));
                }
            }
            for (int toStateId : state.getEpsilonTransitions()) {
                resultAFN.addEpsilonTransition(fromStateId, stateMap.get(toStateId));
            }
        }

        // Transición directa para caso de "cero ocurrencias"
        resultAFN.addEpsilonTransition(startState.getId(), acceptState.getId());

        // Copiar tipo de token desde el primer estado de aceptación original (si tiene)
        for (int oldAcceptStateId : afn.getAcceptingStates()) {
            String tokenType = afn.getState(oldAcceptStateId).getTokenType();
            if (tokenType != null) {
                acceptState.setTokenType(tokenType);
                break; // solo necesitamos copiar uno
            }
        }

        return resultAFN;
    }


    public static AFN ERtoAFN(String regex, String tokenType) {
        Stack<AFN> operandStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();

        // Función auxiliar para aplicar un operador binario
        Runnable applyBinaryOperator = () -> {
            if (operatorStack.isEmpty() || operandStack.size() < 2) {
                return; // Evitar errores si las pilas no tienen suficientes elementos
            }
            char operator = operatorStack.pop();
            AFN operand2 = operandStack.pop();
            AFN operand1 = operandStack.pop();
            switch (operator) {
                case '.': // Concatenación
                    operandStack.push(constructForConcatenation(operand1, operand2));
                    break;
                case '|': // Unión
                    operandStack.push(constructForUnion(operand1, operand2));
                    break;
            }
        };

        // Precedencia de operadores: | (menor), . (medio), *, +, ? (mayor)
        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('|', 1);
        precedence.put('.', 2);
        precedence.put('*', 3);
        precedence.put('+', 3);
        precedence.put('?', 3);

        // Insertar operador de concatenación explícito donde sea necesario
        StringBuilder modifiedRegex = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char currentChar = regex.charAt(i);
            modifiedRegex.append(currentChar);
            if (i + 1 < regex.length()) {
                char nextChar = regex.charAt(i + 1);
                if ((Character.isLetterOrDigit(currentChar) || currentChar == ')' || currentChar == '*' || currentChar == '+' || currentChar == '?') &&
                        (Character.isLetterOrDigit(nextChar) || nextChar == '(')) {
                    modifiedRegex.append('.'); // Insertar concatenación explícita
                }
            }
        }
        regex = modifiedRegex.toString();

        for (char token : regex.toCharArray()) {
            switch (token) {
                case '(':
                    operatorStack.push(token);
                    break;
                case ')':
                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                        applyBinaryOperator.run();
                    }
                    if (!operatorStack.isEmpty() && operatorStack.peek() == '(') {
                        operatorStack.pop(); // Pop el '('
                    }
                    break;
                case '*':
                case '+':
                case '?':
                    if (!operandStack.isEmpty()) {
                        AFN operand = operandStack.pop();
                        if (token == '*') {
                            operandStack.push(constructForKleeneStar(operand));
                        } else if (token == '+') {
                            operandStack.push(constructForPositiveClosure(operand));
                        } else if (token == '?') {
                            operandStack.push(constructForOptional(operand));
                        }
                    }
                    break;
                case '|':
                case '.':
                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(' &&
                            precedence.getOrDefault(operatorStack.peek(), 0) >= precedence.getOrDefault(token, 0)) {
                        applyBinaryOperator.run();
                    }
                    operatorStack.push(token);
                    break;
                default: // Símbolo del alfabeto
                    AFN symbolAFN = new AFN();
                    constructForSymbol(symbolAFN, token, tokenType);
                    operandStack.push(symbolAFN);
                    break;
            }
        }

        while (!operatorStack.isEmpty()) {
            applyBinaryOperator.run();
        }

        if (operandStack.size() == 1) {
            return operandStack.pop();
        } else {
            return new AFN(); // En caso de error en la expresión regular
        }
    }

    private static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}