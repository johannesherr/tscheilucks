package lucks;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	private final Environment enclosing;

	public Environment() {
		this.enclosing = null;
	}

	public Environment(Environment enclosing) {
		this.enclosing = enclosing;
	}

	private final Map<String, Object> data = new HashMap<>();

	public void define(String name, Object value) {
		data.put(name, value);
	}

	public void assign(Token name, Object value) {
		if (data.containsKey(name.getLexeme())) {
			data.put(name.getLexeme(), value);
			return;
		}

		throw new RuntimeError(name, String.format("Undefined variable '%s'", name.getLexeme()));
	}

	public Object get(Token name) {
		String identifier = name.getLexeme();
		if (data.containsKey(identifier)) {
			return data.get(identifier);
		}

		throw new RuntimeError(name, String.format("Undefined variable '%s'.", identifier));
	}

	public Object get(String name) {
		return data.get(name);
	}

	private Environment ancestor(Integer distance) {
		if (distance == 0) return this;
		else return enclosing.ancestor(distance - 1);
	}

	public Object getAt(Token name, Integer distance) {
		return ancestor(distance).get(name);
	}

	public Object getAt(String name, Integer distance) {
		return ancestor(distance).get(name);
	}

	public void assignAt(Integer distance, Token name, Object value) {
		ancestor(distance).assign(name, value);
	}
}
