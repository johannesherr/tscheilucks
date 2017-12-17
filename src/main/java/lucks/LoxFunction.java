package lucks;

import java.util.List;

import lucks.visitors.Interpreter;

public class LoxFunction implements LoxCallable {

	private final Stmt.FunDecl fun;
	private final Environment environment;
	private final boolean isConstructor;

	public LoxFunction(Stmt.FunDecl stmt, Environment environment) {
		this(stmt, environment, false);
	}

	public LoxFunction(Stmt.FunDecl method, Environment environment, boolean isConstructor) {
		this.fun = method;
		this.environment = environment;
		this.isConstructor = isConstructor;
	}

	@Override
	public int arity() {
		return fun.parameters.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> arguments) {
		Environment callEnv = new Environment(environment);
		for (int i = 0; i < arguments.size(); i++) {
			callEnv.define(fun.parameters.get(i).getLexeme(), arguments.get(i));
		}

		Object resultValue = null;
		try {
			interpreter.executeBlock(fun.body, callEnv);
		} catch (Return ret) {
			resultValue = ret.getVal();
		}
		if (isConstructor()) {
			resultValue = environment.get("this");
		}
		return resultValue;
	}

	@Override
	public String toString() {
		return String.format("<fun %s>", fun.name.getLexeme());
	}

	public LoxFunction bind(LoxInstance loxInstance) {
		Environment environment = new Environment(this.environment);
		environment.define("this", loxInstance);
		return new LoxFunction(fun, environment, isConstructor);
	}

	public boolean isConstructor() {
		return isConstructor;
	}
}
