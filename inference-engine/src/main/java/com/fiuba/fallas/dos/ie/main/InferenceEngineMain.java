package com.fiuba.fallas.dos.ie.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.fiuba.fallas.dos.ie.model.Parameters;
import com.fiuba.fallas.dos.ie.model.Rule;

public class InferenceEngineMain {

	static List<Rule> rules = new ArrayList<Rule>();
	static Parameters parameters;

	public static void main(String[] args) {
		loadRules();
		loadParameters();
		evaluateRules();
	}

	private static void evaluateRules() {
		for (Rule rule : rules) {
			Boolean result = rule.action(parameters);
			if (result) {
				System.out.println("Se cumple la Regla " + rule.getNumber() + " resultado = " + rule.getResultado());
			}
		}

	}

	private static void loadParameters() {
		parameters = new Parameters();
		parameters.add("A", Boolean.TRUE);
		parameters.add("B", Boolean.TRUE);
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
}
