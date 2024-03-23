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

        minimizeHelper(afn, dfa, initStates, stateMapping);

        System.out.println("Final states: " + dfa.getFinalStates());
        dfa.generateDOT("dfa.dot");
    }
    private static void minimizeHelper(NondeterministicAutomaton afn, NondeterministicAutomaton dfa, Set<Integer> states, Map<Set<Integer>, Integer> stateMapping) {
        if (states.isEmpty()) return;
        Set<String> alphabets = afn.getAlphabet();
        for (String symbol : alphabets) {
            if (symbol.equals("ε")) continue;
            Set<Integer> nextStates = getNextStates(afn, states, symbol);
            if (!nextStates.isEmpty()) {
                int toStatesId = getStateID(nextStates, stateMapping);
                int fromStatesId = getStateID(states, stateMapping);
                if (!dfa.transitionExist(fromStatesId, symbol, toStatesId)) {
                    dfa.addTransition(fromStatesId, symbol, toStatesId);
                    if (afn.containsFinalState(nextStates)) {
                        System.out.println("Final state found!! " + nextStates + " " + toStatesId);
                        dfa.addFinalState(toStatesId);
                    }
                    if (!nextStates.equals(states)) {
                        System.out.println("Calling Minimize " + states + " symbols " + symbol);
                        minimizeHelper(afn, dfa, nextStates, stateMapping);
                    }
                }
            }
            System.out.println("Next state: " + states + " -" + symbol + "> " + nextStates);
        }
    }

    private static Set<Integer> getNextStates(NondeterministicAutomaton afn, Set<Integer> states, String symbol) {
        Set<Integer> nextStates = new HashSet<>();
        for (int state : states) {
            Map<String, Set<Integer>> transitions = afn.getTransitions(state);
            if (transitions != null) {
                Set<Integer> symbolsTransitions = transitions.getOrDefault(symbol, Collections.emptySet());
                for (int nextState : symbolsTransitions) {
                    nextStates.addAll(epsilonClosure(afn, nextState));
                }
            }
        }
        return nextStates;
    }


    private static Set<Integer> epsilonClosure(NondeterministicAutomaton afn, int state) {
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(state);
        while (!stack.isEmpty()) {
            int currentState = stack.pop();
            visited.add(currentState);
            Map<String, Set<Integer>> transitions = afn.getTransitions(currentState);
            if (transitions != null) {
                Set<Integer> epsilonTransitions = transitions.getOrDefault("ε", Collections.emptySet());
                for (int nextState : epsilonTransitions) {
                    if (!visited.contains(nextState)) {
                        stack.push(nextState);
                    }
                }
            }
        }
        return visited;
    }


    private static int getStateID(Set<Integer> states, Map<Set<Integer>, Integer> stateMapping) {
        if (!stateMapping.containsKey(states)) {
            stateMapping.put(states, stateMapping.size());
        }
        return stateMapping.get(states);
    }
}
