package com.abdokin;

import java.util.List;

public class AutomateBuilder {
    final private NondeterministicAutomaton afn;
    private final RegexParser regexParser;

    public AutomateBuilder(NondeterministicAutomaton afn, RegexParser regexParser) {
        this.afn = afn;
        this.regexParser = regexParser;
    }

    public void build() throws Exception {
        RegexNode regexNode = regexParser.parse();
        int state = 0;
        afn.addInitialState(state);
        int finalState = buildFromNode(regexNode, state);
        afn.addFinalState(finalState);
    }

    private int buildCharacterNode(CharacterNode characterNode, int state) {
        afn.addTransition(state, characterNode.value() + "", ++state);
        return state;
    }

    private int buildConcatenationNode(ConcatenationNode concatenationNode, int state) {
        List<RegexNode> children = concatenationNode.children();
        for (RegexNode child : children) {
            state = buildFromNode(child, state);
        }
        return state;
    }

    private int buildRepetition(RepetitionNode repetitionNode, int state) {
        int current = state + 1;
        afn.addTransition(state, "ε", ++state);
        int new_state = buildFromNode(repetitionNode.child(), state);
        afn.addTransition(new_state, "ε", current);
        afn.addTransition(current, "ε", ++new_state);
        return new_state;
    }

    private int buildAlternationNode(AlternationNode alternationNode, int state) {
        assert alternationNode.children().size() == 2;
        int tmp = state;
        afn.addTransition(tmp, "ε", ++state);

        int lhs = buildFromNode(alternationNode.children().get(0), state);
        afn.addTransition(tmp, "ε", lhs + 1);
        int rhs = buildFromNode(alternationNode.children().get(1), lhs + 1);
        afn.addTransition(lhs, "ε", rhs + 1);
        afn.addTransition(rhs, "ε", rhs + 1);
        return rhs + 1;
    }

    private int buildFromNode(RegexNode node, int state) {
        if (node instanceof CharacterNode characterNode) {
            return buildCharacterNode(characterNode, state);
        } else if (node instanceof RepetitionNode repetitionNode) {
            return buildRepetition(repetitionNode, state);
        } else if (node instanceof AlternationNode alternationNode) {
            return buildAlternationNode(alternationNode, state);
        } else if (node instanceof ConcatenationNode concatenationNode) {
            return buildConcatenationNode(concatenationNode, state);
        }
        return state;
    }
}
