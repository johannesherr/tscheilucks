package lucks;

import lucks.visitors.Interpreter;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {

	private final String name;
	private final LoxClass superClass;
	private final Map<String, LoxFunction> methods;
	private final LoxFunction constructor;

	public LoxClass(String name, LoxClass superClass, Map<String, LoxFunction> methods) {
		this.name = name;
		this.superClass = superClass;
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
		LoxFunction loxFunction = methods.get(name);
		if (loxFunction != null) {
			return loxFunction;
		}

		if (superClass != null) {
			return superClass.findMethod(name);
		}

		return null;
	}

	public LoxFunction findMethod(LoxInstance instance, String name) {
		LoxFunction method = findMethod(name);
		if (method != null) {
			return method.bind(instance);
		}
		return null;
	}
}
