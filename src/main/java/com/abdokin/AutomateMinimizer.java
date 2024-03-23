package com.abdokin;

import java.util.*;

public class AutomateMinimizer {

    public static void minimize(NondeterministicAutomaton afn) {
        Set<Integer> initStates = afn.getInitialState();
        NondeterministicAutomaton dfa = new NondeterministicAutomaton();
        Map<Set<Integer>, Integer> stateMapping = new HashMap<>();

        // Add initial states to the DFA
        int initialDFAState = getStateID(initStates, stateMapping);
        dfa.addInitialState(initialDFAState);

        minimizeHelper(afn, dfa, initStates, stateMapping, new HashSet<>());

        System.out.println("Final states: " + dfa.getFinalStates());
        dfa.generateDOT("dfa.dot");
    }

    private static void minimizeHelper(NondeterministicAutomaton afn, NondeterministicAutomaton dfa,
            Set<Integer> states, Map<Set<Integer>, Integer> stateMapping, Set<Integer> visited) {
        if (states.isEmpty())
            return;
        Set<String> alphabets = afn.getAlphabet();

        System.out.println("Processing states: " + states);
        for (String symbol : alphabets) {
            if (symbol.equals("ε"))
                continue;
            System.out.println("Processing symbol: " + symbol);
            Set<Integer> nextStates = afn.getSymbolTransitions(states, symbol);
            if (nextStates.isEmpty())
                continue;
            System.out.println("states : " + states + " -> " + symbol + " next " + nextStates);

            int toStatesId = getStateID(nextStates, stateMapping);
            int fromStatesId = getStateID(states, stateMapping);
            if (!dfa.transitionExist(fromStatesId, symbol, toStatesId)) {
                dfa.addTransition(fromStatesId, symbol, toStatesId);
                // Check if any final state is reached
                if (afn.containsFinalState(afn.epsilonClosure(nextStates))) {
                    // System.out.println("Final state found: " + nextStates);
                    dfa.addFinalState(toStatesId);
                }
                if (afn.containsFinalState(afn.epsilonClosure(states))) {
                    // System.out.println("Final state found: " + nextStates);
                    dfa.addFinalState(fromStatesId);
                }
                if (!visited.contains(toStatesId)) {
                    System.out.println("Adding new state: " + nextStates + " with ID " +
                            toStatesId);
                    visited.add(toStatesId);
                    minimizeHelper(afn, dfa, nextStates, stateMapping, visited);
                }
            }

        }
    }

    private static int getStateID(Set<Integer> states, Map<Set<Integer>, Integer> stateMapping) {
        if (!stateMapping.containsKey(states)) {
            stateMapping.put(states, stateMapping.size());
        }
        return stateMapping.get(states);
    }
}
