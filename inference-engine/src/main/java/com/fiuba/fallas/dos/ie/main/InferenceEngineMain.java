package com.fiuba.fallas.dos.ie.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.BaseStream;
import java.util.stream.Stream;

import com.fiuba.fallas.dos.ie.model.Parameters;
import com.fiuba.fallas.dos.ie.model.Rule;

public class InferenceEngineMain {

	static List<Rule> rules = new ArrayList<Rule>();
	static Parameters parameters;
	static Parameters knowledgeBase;

	public static void main(String[] args) {
		loadRules();
		System.out.println("Conocimiento inicial:\n");
		loadParameters();
		System.out.println("\nEncadenamiento hacia adelante:\n");
		evaluateRules();
		System.out.println("\nEncadenamiento hacia atrás:\n");
		runBackward("U"); //no verifica
		System.out.println();
		runBackward("V"); //verifica
		System.out.println("");
		runBackward("E"); //verifica con OR
	}

	private static void evaluateRules() {
		for (Rule rule : rules) {
			Boolean ruleContainsAllParameters = true;
			for(String hypothesis : rule.getAllHypothesis()) {
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
				}
			}
		}
	}

	private static void loadParameters() {
		parameters = new Parameters();

		parameters.add("Q", Boolean.TRUE);
		System.out.println("Q = True");

		parameters.add("P", Boolean.TRUE);
		System.out.println("P = True");

		parameters.add("R", Boolean.TRUE);
		System.out.println("R = True");
	}

	private static void loadRules() {
		String fileName = "./src/main/resources/Rules.txt";

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			stream.forEach(InferenceEngineMain::buildRule);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void buildRule(String line) {
		Rule rule = new Rule(rules.size());
		rule.build(line);
		rules.add(rule);
	}

	private static void runBackward(String conclusionToVerify) {
		knowledgeBase = parameters;
		Parameters p = verifyConclusion(conclusionToVerify);
		if (p.getValue(conclusionToVerify) == Boolean.FALSE) {
			System.out.println("No se pudo verificar " + conclusionToVerify);
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
