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
