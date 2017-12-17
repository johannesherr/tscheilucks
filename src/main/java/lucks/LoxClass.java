package lucks;

import java.util.List;
import java.util.Map;

import lucks.visitors.Interpreter;

public class LoxClass implements LoxCallable {

	private final String name;
	private final Map<String, LoxFunction> methods;

	public LoxClass(String name, Map<String, LoxFunction> methods) {
		this.name = name;
		this.methods = methods;
	}

	@Override
	public String toString() {
		return "LoxClass{" +
						"name='" + name + '\'' +
						'}';
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> params) {
		return new LoxInstance(this);
	}

	@Override
	public int arity() {
		return 0;
	}


	public LoxFunction findMethod(String name) {
		return methods.get(name);
	}
}
