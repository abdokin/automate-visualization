package com.abdokin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

record ConcatenationNode(List<RegexNode> children) implements RegexNode {
    @Override
    public String getType() {
        return "Concatenation";
    }

    @Override
    public String toString() {
        return getType() + "{" + children.stream()
                .map(Object::toString)
                .collect(Collectors.joining("")) + "}";
    }
}

record AlternationNode(List<RegexNode> children) implements RegexNode {
    @Override
    public String getType() {
        return "Alternation";
    }

    @Override
    public String toString() {
        return children.stream()
                .map(Object::toString)
                .collect(Collectors.joining("|", "(", ")"));
    }
}

record RepetitionNode(RegexNode child) implements RegexNode {
    @Override
    public String getType() {
        return "Repetition";
    }

    @Override
    public String toString() {
        return getType() + "{ " + child + " }";
    }
}

record CharacterNode(char value) implements RegexNode {
    @Override
    public String getType() {
        return "Character";
    }

    @Override
    public String toString() {
        return getType() + "{" + value + "}";
    }
}

public class RegexParser {
    private final String regex;
    private int index;

    public RegexParser(String regex) {
        this.regex = regex;
        this.index = 0;
    }

    public RegexNode parse() throws Exception {
        List<RegexNode> nodes = new ArrayList<>();
        while (index < regex.length() && regex.charAt(index) != ')') {
            var lhs = parseTerm();
            if (index < regex.length() && regex.charAt(index) == '|' && regex.charAt(index) != ')') {
                index++; // Skip '|' character
                var rhs = parseTerm();
                nodes.add(new AlternationNode(Arrays.asList(lhs, rhs)));
            } else {
                nodes.add(lhs);
            }
        }
        if (nodes.size() <= 1) {
            var tmp = nodes.get(0);
            if (tmp instanceof ConcatenationNode concatenationNode && concatenationNode.children().size() == 1) {
                tmp = concatenationNode.children().get(0);
            }
            return tmp;
        } else {
            return new ConcatenationNode(nodes);
        }
    }

    private RegexNode parseTerm() throws Exception {
        List<RegexNode> nodes = new ArrayList<>();
        while (index < regex.length() && regex.charAt(index) != '|' && regex.charAt(index) != ')') {
            var lhs = parseFactor();
            if (index < regex.length() && regex.charAt(index) == '|') {
                index++;
                var rhs = parse();
                nodes.add(new AlternationNode(Arrays.asList(lhs, rhs)));
            } else {
                nodes.add(lhs);
            }
        }
        assert !nodes.isEmpty();
        return new ConcatenationNode(nodes);
    }

    private RegexNode parseFactor() throws Exception {
        var node = parseAtom();
        if (index < regex.length() && regex.charAt(index) == '*') {
            index++; // Skip '*' character
            return new RepetitionNode(node);
        } else
            return Objects.requireNonNullElseGet(node, () -> new CharacterNode('ε'));
    }

    private RegexNode parseAtom() throws Exception {
        if (index < regex.length() && regex.charAt(index) == '(') {
            index++; // Skip '(' character
            var node = parse();
            if (index < regex.length() && regex.charAt(index) == ')') {
                index++; // Skip ')' character
                return node;
            } else {
                throw new Exception("Error: Missing closing parenthesis ')'");
            }
        } else {
            char value = regex.charAt(index++);
            return new CharacterNode(value);
        }
    }
}
