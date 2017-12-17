package lucks;

import java.util.List;
import java.util.Map;

import lucks.visitors.Interpreter;

public class LoxClass implements LoxCallable {

	private final String name;
	private final Map<String, LoxFunction> methods;
	private final LoxFunction constructor;

	public LoxClass(String name, Map<String, LoxFunction> methods) {
		this.name = name;
		this.methods = methods;
		this.constructor = methods.values().stream()
						.filter(LoxFunction::isConstructor)
						.findAny()
						.orElse(null);
	}

	@Override
	public String toString() {
		return "LoxClass{" +
						"name='" + name + '\'' +
						'}';
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		LoxInstance instance = new LoxInstance(this);
		if (constructor != null) {
			constructor.bind(instance).call(interpreter, arguments);
		}
		return instance;
	}

	@Override
	public int arity() {
		return constructor != null ? constructor.arity() : 0;
	}


	public LoxFunction findMethod(String name) {
		return methods.get(name);
	}
}
