package lucks;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {

	private final LoxClass loxClass;
	private final Map<String, Object> fields = new HashMap<>();

	public LoxInstance(LoxClass loxClass) {
		this.loxClass = loxClass;
	}

	@Override
	public String toString() {
		return "LoxInstance{" +
						"loxClass=" + loxClass +
						'}';
	}

	public Object get(Token name) {
		String field = name.getLexeme();
		Object val = fields.get(field);
		if (val != null) {
			return val;
		}

		LoxFunction method = loxClass.findMethod(this, field);
		if (method != null) {
			return method;
		}

		throw new RuntimeError(name, "Undefined property: " + field);
	}

	public void put(Token name, Object value) {
		fields.put(name.getLexeme(), value);
	}
}
