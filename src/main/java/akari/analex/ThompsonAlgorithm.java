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
        State state = afn.getState(acceptState.getId());
        state.setTokenType(tokenType);
        afn.tokenTypes.put(acceptState.getId(), tokenType);
    }
    return afn;
}

    static AFN constructForConcatenation(AFN afn1, AFN afn2) {
    AFN resultAFN = new AFN();
    
    // 1. Mapeo y copia de estados de afn1
    Map<Integer, Integer> afn1StateMap = new HashMap<>();
    for (State state : afn1.getStates().values()) {
        int newStateId = resultAFN.createState().getId();
        afn1StateMap.put(state.getId(), newStateId);
        
        // Establecer estado inicial
        if (state.getId() == afn1.getStartState()) {
            resultAFN.setStartState(newStateId);
        }
    }
    
    // 2. Mapeo y copia de estados de afn2
    Map<Integer, Integer> afn2StateMap = new HashMap<>();
    int afn2StartInResult = -1;
    for (State state : afn2.getStates().values()) {
        int newStateId = resultAFN.createState().getId();
        afn2StateMap.put(state.getId(), newStateId);
        
        if (state.getId() == afn2.getStartState()) {
            afn2StartInResult = newStateId;
        }
    }
    
    // 3. Copiar todas las transiciones de afn1
    for (State state : afn1.getStates().values()) {
        int from = afn1StateMap.get(state.getId());
        
        // Transiciones de símbolos
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            for (int to : entry.getValue()) {
                resultAFN.addTransition(from, entry.getKey(), afn1StateMap.get(to));
            }
        }
        
        // Transiciones épsilon
        for (int to : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(from, afn1StateMap.get(to));
        }
    }
    
    // 4. Copiar todas las transiciones de afn2
    for (State state : afn2.getStates().values()) {
        int from = afn2StateMap.get(state.getId());
        
        // Transiciones de símbolos
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            for (int to : entry.getValue()) {
                resultAFN.addTransition(from, entry.getKey(), afn2StateMap.get(to));
            }
        }
        
        // Transiciones épsilon
        for (int to : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(from, afn2StateMap.get(to));
        }
    }
    
    // 5. Conectar estados de aceptación de afn1 al inicio de afn2
    for (int acceptState : afn1.getAcceptingStates()) {
        resultAFN.addEpsilonTransition(
            afn1StateMap.get(acceptState),
            afn2StartInResult
        );
    }
    
    // 6. Establecer estados de aceptación (solo los de afn2)
    for (int acceptState : afn2.getAcceptingStates()) {
        int mappedAccept = afn2StateMap.get(acceptState);
        resultAFN.addAcceptingState(mappedAccept);
        
        // Copiar tokentype manteniendo consistencia
        String tokenType = afn2.getState(acceptState).getTokenType();
        
        if (tokenType != null) {
            resultAFN.getState(mappedAccept).setTokenType(tokenType); // Forzar consistencia
        }
        
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
/*
    static AFN constructForPositiveClosure(AFN afn) {
        AFN afnCopy = afn.clone(); // Asegúrate de tener un método clone() que haga una copia profunda
        AFN afnStar = constructForKleeneStar(afn);
        return constructForConcatenation(afnCopy, afnStar);
    }
*/
   static AFN constructForPositiveClosure(AFN afn) {
    AFN resultAFN = new AFN();
    
    // Crear nuevo estado inicial y de aceptación
    State newStart = resultAFN.createState();
    State newAccept = resultAFN.createState();
    resultAFN.setStartState(newStart.getId());
    resultAFN.addAcceptingState(newAccept.getId());

    // Mapear estados originales
    Map<Integer, Integer> stateMap = new HashMap<>();
    for (State state : afn.getStates().values()) {
        int newId = resultAFN.createState().getId();
        stateMap.put(state.getId(), newId);
    }

    // Conectar nuevo inicio al inicio original
    resultAFN.addEpsilonTransition(newStart.getId(), stateMap.get(afn.getStartState()));

    // Copiar todas las transiciones del AFN original
    for (State state : afn.getStates().values()) {
        int from = stateMap.get(state.getId());
        
        // Copiar transiciones de símbolos
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            for (int to : entry.getValue()) {
                resultAFN.addTransition(from, entry.getKey(), stateMap.get(to));
            }
        }
        
        // Copiar transiciones épsilon
        for (int to : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(from, stateMap.get(to));
        }
        
        // Conectar estados de aceptación originales al nuevo aceptación y al inicio original
        if (afn.getAcceptingStates().contains(state.getId())) {
            resultAFN.addEpsilonTransition(from, newAccept.getId());
            resultAFN.addEpsilonTransition(from, stateMap.get(afn.getStartState()));
        }
    }

    // Copiar token del primer estado de aceptación original
    for (int oldAccept : afn.getAcceptingStates()) {
        String tokenType = afn.getState(oldAccept).getTokenType();
        if (tokenType != null) {
            resultAFN.getState(newAccept.getId()).setTokenType(tokenType);
            break;
        }
    }

    return resultAFN;
}

    static AFN constructForOptional(AFN afn) {
    AFN resultAFN = new AFN();
    State newStart = resultAFN.createState();
    State newAccept = resultAFN.createState();
    resultAFN.setStartState(newStart.getId());
    resultAFN.addAcceptingState(newAccept.getId());
    // 1. Camino vacío
    resultAFN.addEpsilonTransition(newStart.getId(), newAccept.getId());
    // 2. Mapeo de estados originales
    Map<Integer, Integer> stateMap = new HashMap<>();
    for (State state : afn.getStates().values()) {
        int newStateId = resultAFN.createState().getId();
        stateMap.put(state.getId(), newStateId);
        
        if (state.getId() == afn.getStartState()) {
            // Conectar nuevo inicio al inicio original
            resultAFN.addEpsilonTransition(newStart.getId(), newStateId);
        }
        if (afn.getAcceptingStates().contains(state.getId())) {
            // Conectar estados de aceptación originales al nuevo acept
            resultAFN.addEpsilonTransition(newStateId, newAccept.getId());
        }
    }

    // 4. Copiar todas las transiciones del AFN original
    for (State state : afn.getStates().values()) {
        int from = stateMap.get(state.getId());
        
        // Transiciones de símbolos
        for (Map.Entry<Character, Set<Integer>> entry : state.getTransitions().entrySet()) {
            for (int to : entry.getValue()) {
                resultAFN.addTransition(from, entry.getKey(), stateMap.get(to));
            }
        }
        
        // Transiciones épsilon
        for (int to : state.getEpsilonTransitions()) {
            resultAFN.addEpsilonTransition(from, stateMap.get(to));
        }
    }

    // 5. Manejo de tokens (sin forzar "NUMEROS")
    if (!afn.getAcceptingStates().isEmpty()) {
        String originalToken = afn.getState(afn.getAcceptingStates().iterator().next()).getTokenType();
        newAccept.setTokenType(originalToken);
    }

    return resultAFN;
}


    public static AFN ERtoAFN(String regex, String tokenType) {
    Stack<AFN> operandStack = new Stack<>();
    Stack<Character> operatorStack = new Stack<>();
    
    // Función auxiliar para aplicar un operador binario
    Runnable applyBinaryOperator = () -> {
        if (operatorStack.isEmpty() || operandStack.size() < 2) return;
        char operator = operatorStack.pop();
        AFN operand2 = operandStack.pop();
        AFN operand1 = operandStack.pop();
        switch (operator) {
            case '.': operandStack.push(constructForConcatenation(operand1, operand2)); break;
            case '|': operandStack.push(constructForUnion(operand1, operand2)); break;
        }
    };

    // Precedencia de operadores
    Map<Character, Integer> precedence = new HashMap<>();
    precedence.put('|', 1);
    precedence.put('.', 2);
    precedence.put('*', 3);
    precedence.put('+', 3);
    precedence.put('?', 3);

    // Procesamiento de rangos [a-z] y [0-9]
    StringBuilder modifiedRegex = new StringBuilder();
    boolean inRange = false;
    char rangeStart = 0;
    boolean rangeFirstChar = false;
    
    for (int i = 0; i < regex.length(); i++) {
        char currentChar = regex.charAt(i);
        
        if (currentChar == '[' && !inRange) {
            inRange = true;
            rangeFirstChar = true;
            continue;
        } else if (currentChar == ']' && inRange) {
            inRange = false;
            // Reemplazar el rango con un carácter especial temporal
            modifiedRegex.append('Ω'); // Usamos Ω como marcador de posición
            continue;
        }
        
        if (inRange) {
            if (currentChar == '-' && !rangeFirstChar) {
                // Es un rango como a-z
                i++;
                char endChar = regex.charAt(i);
                AFN rangeAFN = constructForRange(rangeStart, endChar, tokenType);
                operandStack.push(rangeAFN);
                rangeFirstChar = false;
            } else {
                rangeStart = currentChar;
                rangeFirstChar = false;
            }
            continue;
        }
        
        modifiedRegex.append(currentChar);
        
        // Insertar operador de concatenación implícito
        if (i + 1 < regex.length()) {
            char nextChar = regex.charAt(i + 1);
            if ((Character.isLetterOrDigit(currentChar) || currentChar == ')' || 
                 currentChar == '*' || currentChar == '+' || currentChar == '?') &&
                (Character.isLetterOrDigit(nextChar) || nextChar == '(')) {
                modifiedRegex.append('.');
            }
        }
    }
    
    regex = modifiedRegex.toString();
    
    // Procesar la expresión regular modificada
    for (int i = 0; i < regex.length(); i++) {
        char token = regex.charAt(i);
        
        switch (token) {
            case 'Ω': // Nuestro marcador de posición para rangos
                // Ya pusimos el AFN del rango en la pila durante el preprocesamiento
                break;
            case '(':
                operatorStack.push(token);
                break;
            case ')':
                while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                    applyBinaryOperator.run();
                }
                if (!operatorStack.isEmpty() && operatorStack.peek() == '(') {
                    operatorStack.pop();
                }
                break;
            case '*':
            case '+':
            case '?':
                if (!operandStack.isEmpty()) {
                    AFN operand = operandStack.pop();
                    switch (token) {
                        case '*': operandStack.push(constructForKleeneStar(operand)); break;
                        case '+': operandStack.push(constructForPositiveClosure(operand)); break;
                        case '?': operandStack.push(constructForOptional(operand)); break;
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
            default:
                AFN symbolAFN = new AFN();
                constructForSymbol(symbolAFN, token, tokenType);
                operandStack.push(symbolAFN);
                break;
        }
    }

    while (!operatorStack.isEmpty()) {
        applyBinaryOperator.run();
    }

    return operandStack.size() == 1 ? operandStack.pop() : new AFN();
}

    private static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    private static AFN constructForRange(char start, char end, String tokenType) {
        AFN resultAFN = new AFN();
        //AFN dotAFN = constructForRange('\u0000', '\uFFFF', null);  // Rango Unicode completo 
        State startState = resultAFN.createState();
        State acceptState = resultAFN.createState();
    
        resultAFN.setStartState(startState.getId());
        resultAFN.addAcceptingState(acceptState.getId());
        
        // Añadir transiciones para cada carácter en el rango
        for (char c = start; c <= end; c++) {
            resultAFN.addTransition(startState.getId(), c, acceptState.getId());
        }
        
        if (tokenType != null) {
            resultAFN.getState(acceptState.getId()).setTokenType(tokenType);
        }
    
        return resultAFN;
    }
}