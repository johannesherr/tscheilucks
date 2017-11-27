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

		if (enclosing != null) {
			enclosing.assign(name, value);
			return;
		}

		throw new RuntimeError(name, String.format("Undefined variable '%s'", name.getLexeme()));
	}

	public Object get(Token name) {
		String identifier = name.getLexeme();
		if (data.containsKey(identifier)) {
			return data.get(identifier);
		}

		if (enclosing != null) {
			return enclosing.get(name);
		}

		throw new RuntimeError(name, String.format("Undefined variable '%s'.", identifier));
	}
}
