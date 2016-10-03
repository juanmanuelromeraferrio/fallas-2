package com.fiuba.fallas.dos.ie.model;

import java.util.HashMap;
import java.util.Map;

public class Parameters {

	private Map<String, Boolean> map;

	public Parameters() {
		super();
		map = new HashMap<String, Boolean>();
	}

	public Boolean getValue(String key) {
		return this.map.get(key);
	}

	public void add(String key, Boolean value) {
		this.map.put(key, value);
	}

}
