package com.abdokin;
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java Main <regex>");
            return;
        }
        String regex = args[0];
        NondeterministicAutomaton afn = new NondeterministicAutomaton();
        AutomateBuilder builder = new AutomateBuilder(afn, new RegexParser(regex));
        builder.build();
        String fileName = "afn.dot";
        afn.generateDOT(fileName);
        AutomateMinimizer.minimize(afn);

    }
}
