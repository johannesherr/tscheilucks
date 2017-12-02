package lucks;

import java.util.List;

import lucks.visitors.Interpreter;

public class LoxFunction implements LoxCallable {

	private final Stmt.FunDecl fun;
	private final Environment environment;

	public LoxFunction(Stmt.FunDecl stmt, Environment environment) {
		this.fun = stmt;
		this.environment = environment;
	}

	@Override
	public int arity() {
		return fun.parameters.size();
	}

	@Override
	public Object call(Interpreter interpreter, List<Object> params) {
		Environment callEnv = new Environment(environment);
		for (int i = 0; i < params.size(); i++) {
			callEnv.define(fun.parameters.get(i).getLexeme(), params.get(i));
		}

		try {
			interpreter.executeBlock(fun.body, callEnv);
			return null;
		} catch (Return ret) {
			return ret.getVal();
		}
	}

	@Override
	public String toString() {
		return String.format("<fun %s>", fun.name.getLexeme());
	}
}
