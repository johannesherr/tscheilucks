package lucks;

import java.util.HashMap;
import java.util.Map;

public class Environment {

	private final Map<String, Object> data = new HashMap<>();

	public void define(String name, Object value) {
		data.put(name, value);
	}

	public Object get(Token name) {
		String identifier = name.getLexeme();
		if (data.containsKey(identifier)) {
			return data.get(identifier);
		} else {
			throw new RuntimeError(name, "Undefined variable '%s'.".format(identifier));
		}
	}
}
