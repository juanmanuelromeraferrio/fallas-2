package com.fiuba.fallas.dos.ie.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Rule {

	private static final String AND = "AND";
	private static final String OR = "OR";

	private Integer number;
	private String rule;
	private String condition;
	private List<String> parameters;
	private List<BiFunction<Boolean, Boolean, Boolean>> conditions;

	public Rule(Integer number) {
		super();
		this.number = number;
		this.parameters = new ArrayList<String>();
		this.conditions = new ArrayList<BiFunction<Boolean, Boolean, Boolean>>();
	}

	public Integer getNumber() {
		return this.number;
	}

	public String getAsString() { return this.rule; }

	public String getCondition() { return this.condition; }

	public String getResultado() {
		return parameters.get(parameters.size() - 1);
	}

	public void build(String line) {
		rule = line;
		String[] array = line.split(" ");
		for (int i = 0; i < array.length; i++) {
			String word = array[i];
			if (i % 2 == 0) {
				parameters.add(word);
			} else {
				if (word.equals(AND)) {
					this.condition = "AND";
					conditions.add(new BiFunction<Boolean, Boolean, Boolean>() {

						public Boolean apply(Boolean x, Boolean y) {
							Boolean result = x && y;
							//System.out.println("x=" + x + " AND y=" + y + " => " + result);
							return result;
						}
					});

				} else if (word.equals(OR)) {
					this.condition = "OR";
					conditions.add(new BiFunction<Boolean, Boolean, Boolean>() {

						public Boolean apply(Boolean x, Boolean y) {
							Boolean result = x || y;
							//System.out.println("x=" + x + " OR y=" + y + " => " + result);
							return result;
						}
					});
				}
			}
		}
	}

	public Boolean action(Parameters values) {
		int j = 0;
		Boolean result = null;
		for (BiFunction<Boolean, Boolean, Boolean> condition : conditions) {
			if (result == null) {
				String parameter_1 = parameters.get(j);
				String parameter_2 = parameters.get(j + 1);
				Boolean value_1 = values.getValue(parameter_1);
				Boolean value_2 = values.getValue(parameter_2);
				j += 2;
				result = condition.apply(value_1, value_2);
			} else {

				String parameter_1 = parameters.get(j);
				Boolean value_1 = values.getValue(parameter_1);
				j++;
				result = condition.apply(result, value_1);
			}
		}

		System.out.println("Regla " + number + " dio como resultado " + result);
		return result;
	}

	public Boolean containsConclusion(String conclusion) {
		String lastElement = parameters.get(parameters.size() - 1);
		return (lastElement.equals(conclusion));
	}

	public List<String> getAllHypothesis() {
		List<String> hypothesis = new ArrayList<String>();
		for (String param : parameters) {
			if (parameters.indexOf(param) < (parameters.size() - 1)) {
				hypothesis.add(param);
			}
		}
		return hypothesis;
	}
}
