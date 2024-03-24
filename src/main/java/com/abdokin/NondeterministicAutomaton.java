package com.abdokin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class NondeterministicAutomaton {
    private final List<Map<String, Set<Integer>>> transitions = new ArrayList<>();
    private final Set<Integer> states = new HashSet<>();
    private final Set<Integer> finalStates = new HashSet<>();
    private final Set<Integer> initialStates = new HashSet<>();
    private final Set<String> alphabet = new HashSet<>();

    public void addInitialState(int state) {
        states.add(state);
        initialStates.add(state);
    }

    public Map<String, Set<Integer>> getTransitions(int state) {
        return state < transitions.size() ? transitions.get(state) : null;
    }

    private Set<Integer> getEpsilonTransitions(int state) {
        Map<String, Set<Integer>> epsilonTransitions = getTransitions(state);
        return epsilonTransitions != null ? epsilonTransitions.getOrDefault("ε", Collections.emptySet())
                : Collections.emptySet();
    }

    public Set<Integer> getSymbolTransitions(Set<Integer> states, String symbol) {
        Set<Integer> res = new HashSet<>();
        for (int state : states) {
            res.addAll(getSymbolTransitions(state, symbol));
        }
        return res;
    }

    public Set<Integer> getSymbolTransitions(int state, String symbol) {
        Set<Integer> visited = new HashSet<>();
        Set<Integer> symbolTransitions = new HashSet<>();
        Queue<Integer> statesToProcess = new LinkedList<>();

        statesToProcess.add(state);

        while (!statesToProcess.isEmpty()) {
            int currentState = statesToProcess.poll();
            if (!visited.contains(currentState)) {
                visited.add(currentState);
                var transitions = getTransitions(currentState);
                if (transitions != null) {
                    var nextStates = transitions.getOrDefault(symbol, Collections.emptySet());

                    if (!nextStates.isEmpty()) {
                        symbolTransitions.addAll(nextStates);
                        statesToProcess.addAll(nextStates);
                    } else {
                        
                        // Retrieve epsilon transitions for the current state
                        Set<Integer> epsilonTransitions = getEpsilonTransitions(currentState);
                        System.out.println(epsilonTransitions);
                        for (int epsilonState : epsilonTransitions) {
                            if (!visited.contains(epsilonState)) {
                                statesToProcess.add(epsilonState);
                            }
                        }
                    }
                }

            }
        }

        return epsilonClosure(symbolTransitions);
    }

    public boolean transitionExist(int fromState, String symbol, int toState) {
        try {
            Map<String, Set<Integer>> transitionMap = transitions.get(fromState);
            return transitionMap != null && transitionMap.containsKey(symbol)
                    && transitionMap.get(symbol).contains(toState);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean isFinal(int state) {
        return finalStates.contains(state);
    }

    public Set<Integer> epsilonClosure(Set<Integer> states) {
        Set<Integer> res = new HashSet<>();
        for (int state : states) {
            res.addAll(epsilonClosure(state));
        }
        return res;
    }

    public Set<Integer> epsilonClosure(int state) {
        Set<Integer> visited = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(state);
        while (!stack.isEmpty()) {
            int currentState = stack.pop();
            visited.add(currentState);
            Map<String, Set<Integer>> transitions = getTransitions(currentState);
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

    public void addFinalState(int state) {
        finalStates.add(state);
    }

    public void addTransition(int fromState, String symbol, int toState) {
        while (transitions.size() <= fromState) {
            transitions.add(new HashMap<>());
        }
        alphabet.add(symbol);
        Map<String, Set<Integer>> transitionMap = transitions.get(fromState);
        transitionMap.computeIfAbsent(symbol, k -> new HashSet<>()).add(toState);
        states.add(fromState);
        states.add(toState);
    }

    public boolean containsFinalState(Set<Integer> states) {
        return finalStates.stream().anyMatch(states::contains);
    }

    public void generateDOT(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("digraph Automate {\n");

            for (int state : states) {
                if (state == -1)
                    continue;
                writer.write("    " + state);
                if (isFinal(state)) {
                    writer.write(" [shape=doublecircle ");
                    if (initialStates.contains(state)) {
                        writer.write("style=filled, fillcolor=lightblue");
                    }
                    writer.write(" ];\n");
                } else if (initialStates.contains(state)) {
                    writer.write(" [shape=circle, style=filled, fillcolor=lightblue];\n");
                } else {
                    writer.write(" [shape=circle];\n");
                }
            }

            for (int fromState = 0; fromState < transitions.size(); fromState++) {
                Map<String, Set<Integer>> transitionMap = transitions.get(fromState);
                if (transitionMap != null) {
                    for (Map.Entry<String, Set<Integer>> entry : transitionMap.entrySet()) {
                        String symbol = entry.getKey();
                        Set<Integer> toStates = entry.getValue();
                        for (int toState : toStates) {
                            if (toState != -1) {
                                writer.write("    " + fromState + " -> " + toState);
                                writer.write(" [label=\"" + symbol + "\"];\n");
                            }
                        }
                    }
                }
            }

            writer.write("}");
            System.out.println("DOT graph saved to " + fileName);

            String command = "dot -Tpng -o " + fileName.replace(".dot", ".png") + " " + fileName;
            Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public Set<Integer> getInitialState() {
        return initialStates;
    }

    public Set<String> getAlphabet() {
        return alphabet;
    }

    public Set<Integer> getFinalStates() {
        return finalStates;
    }

}
