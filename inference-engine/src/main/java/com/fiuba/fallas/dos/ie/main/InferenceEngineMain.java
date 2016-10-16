package com.fiuba.fallas.dos.ie.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fiuba.fallas.dos.ie.model.Parameters;
import com.fiuba.fallas.dos.ie.model.Rule;
import java.util.Map;
import java.util.Scanner;

public class InferenceEngineMain {

    static List<Rule> rules = new ArrayList<Rule>();
    static Parameters parameters;
    static Parameters knowledgeBase;

    public static void main(String[] args) {
        loadRules();
        loadParameters();
        System.out.println("\nBase de conocimientos inicial cargada.\n");
        showKnoledgeBase();
        String chainingOption = chooseChainingOption();
        if (chainingOption.equals("forward")) {
            System.out.println("\nEncadenamiento hacia adelante:\n");
            runForward();
        } else {
            String target = chooseTargetBackward();
            System.out.println("\nEncadenamiento hacia atrás:\n");
            runBackward(target);
        }
    }

    private static void showKnoledgeBase() {
        System.out.println("Reglas:");
        String fileName = "./src/main/resources/Rules.txt";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nHechos:");
        fileName = "./src/main/resources/Parameters.txt";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String chooseChainingOption() {
        Scanner input = new Scanner(System.in);
        String result = "";
        do {
            System.out.println("\nIngrese el tipo de evaluación de las reglas.\n"
                    + "1. Encadenamiento hacia adelante \n"
                    + "2. Encadenamiento hacia atrás \n");

            result = input.next();
        } while (!(result.equals("1") || result.equals("2")));
        return (result.equals("1")) ? "forward" : "backward";
    }

    private static String chooseTargetBackward() {
        Scanner input = new Scanner(System.in);
        String result = "";
        do {
            System.out.println("\nIngrese el objetivo a verificar: \n");
            result = input.nextLine();
        } while (result.isEmpty());
        return result.toUpperCase();
    }

    private static void runForward() {

        knowledgeBase = parameters;
        List<String> results = new ArrayList<>();
        while (existsDerivableRule()) {
            for (Rule rule : rules) {
                Boolean ruleContainsAllParameters = true;
                for (String hypothesis : rule.getAllHypothesis()) {
                    if (parameters.getValue(hypothesis) == null) {
                        ruleContainsAllParameters = false;
                        break;
                    }
                }
                if (ruleContainsAllParameters) {
                    System.out.println(
                            "Se evalúa la regla " + rule.getNumber()
                            + " (" + rule.getAsString() + ")"
                            + " ya que se cuentan con las premisas necesarias");
                    Boolean result = rule.action(parameters);
                    if (result) {
                        System.out.println("Se cumple la Regla " + rule.getNumber() + " resultado = " + rule.getResultado());
                        results.add(rule.getResultado());
                        knowledgeBase.add(rule.getResultado(), true);
                        rule.setComplete(Boolean.TRUE);
                    }
                }
            }
        }

        System.out.println("\n\n\nBase de conocimientos final\n");
        showKnoledgeBase();
        System.out.println("\nHechos derivados:");
        results.stream().forEach(System.out::println);
    }

    private static Boolean existsDerivableRule() {
        Boolean existDerivableRule = Boolean.FALSE;
        Integer ruleNumber = 0;

        for (Rule rule : rules) {
            if (rule.getComplete()) {
                break;
            }
            existDerivableRule = Boolean.TRUE;
            for (String parameterRule : rule.getAllHypothesis()) {
                if (knowledgeBase.getValue(parameterRule) == null) {
                    existDerivableRule = Boolean.FALSE;
                    break;
                }
            }

            ruleNumber = rule.getNumber();
            if (existDerivableRule) {
                break;
            }

        }
        if (existDerivableRule) {
            System.out.println("Existe regla derivable");
            System.out.println("Regla numero: " + ruleNumber);
        } else {
            System.out.println("No existe regla derivable");
        }

        return existDerivableRule;
    }

    private static void loadRules() {
        String fileName = "./src/main/resources/Rules.txt";

        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(InferenceEngineMain::buildRule);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadParameters() {
        parameters = new Parameters();

        String fileName = "./src/main/resources/Parameters.txt";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            stream.forEach(InferenceEngineMain::buildParameter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void buildRule(String line) {
        Rule rule = new Rule(rules.size());
        rule.build(line);
        rules.add(rule);
    }

    public static void buildParameter(String line) {
        String[] array = line.split(" ");
        String parameterName = array[0];
        Boolean parameterValue = Boolean.TRUE;
        if (array.length >= 2) {
            parameterValue = Boolean.valueOf(array[1]);
        }
        parameters.add(parameterName, parameterValue);
    }

    private static void runBackward(String conclusionToVerify) {
        knowledgeBase = parameters;
        Parameters p = verifyConclusion(conclusionToVerify);
        if (p.getValue(conclusionToVerify)) {
            System.out.println("\n\nEl objetivo '" + conclusionToVerify + "' ha sido verificado");
        } else {
            System.out.println("\n\nNo se pudo verificar el objetivo '" + conclusionToVerify + "'");
        }
    }

    private static Parameters verifyConclusion(String conclusionToVerify) {
        System.out.println("Se intenta verificar " + conclusionToVerify);
        knowledgeBase.add(conclusionToVerify, Boolean.TRUE);
        for (Rule rule : getMatchingRules(conclusionToVerify)) {
            System.out.println("Se intenta verificar con la regla " + rule.getNumber() + " (" + rule.getAsString() + ")");
            List<String> missingHypothesis = getMissingHypothesis(rule);
            if (!missingHypothesis.isEmpty()) {
                for (String missingHyp : missingHypothesis) {
                    System.out.println(missingHyp + " no está contenida en la base de conocimientos");
                    if (!verifyConclusion(missingHyp).getValue(missingHyp)) {
                        Parameters p = new Parameters();
                        p.add(conclusionToVerify, Boolean.FALSE);
                        System.out.println("No se pudo verificar " + missingHyp);
                        if (rule.getCondition() == "OR") {
                            break;
                        } else {
                            return p;
                        }
                    }
                }
            }
            Parameters p = new Parameters();
            System.out.println(
                    "Se evalúa la regla " + rule.getNumber()
                    + " (" + rule.getAsString() + ")"
                    + " ya que se cuentan con las premisas necesarias");
            p.add(conclusionToVerify, rule.action(getParametersOfRule(rule)));
            System.out.println("Se verificó " + conclusionToVerify);
            return p;
        }
        Parameters p = new Parameters();
        p.add(conclusionToVerify, Boolean.FALSE);
        System.out.println("No hay ninguna regla para verificar " + conclusionToVerify);
        return p;
    }

    private static List<Rule> getMatchingRules(String conclusion) {
        List<Rule> matchingRules = new ArrayList<>();
        for (Rule rule : rules) {
            if (rule.containsConclusion(conclusion)) {
                matchingRules.add(rule);
            }
        }
        return matchingRules;
    }

    private static List<String> getMissingHypothesis(Rule rule) {
        List<String> missingHypothesis = new ArrayList<>();
        for (String hypothesis : rule.getAllHypothesis()) {
            if (knowledgeBase.getValue(hypothesis) == null) {
                missingHypothesis.add(hypothesis);
            }
        }
        return missingHypothesis;
    }

    private static Parameters getParametersOfRule(Rule rule) {
        Parameters parametersOfRule = new Parameters();
        for (String hypothesis : rule.getAllHypothesis()) {
            if (knowledgeBase.getValue(hypothesis) != null) {
                parametersOfRule.add(hypothesis, knowledgeBase.getValue(hypothesis));
            }
        }
        return parametersOfRule;
    }

}
