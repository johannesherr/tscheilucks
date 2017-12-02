package lucks.visitors;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import lucks.Environment;
import lucks.Expr;
import lucks.LoxCallable;
import lucks.LoxFunction;
import lucks.Return;
import lucks.RuntimeError;
import lucks.Stmt;
import lucks.Token;
import lucks.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

	private Environment globals = new Environment();
	private Environment environment = globals;

	public Interpreter() {
		globals.define("clock", new LoxCallable() {
			@Override
			public int arity() {
				return 0;
			}

			@Override
			public Double call(Interpreter interpreter, List<Object> params) {
				return (double) System.currentTimeMillis();
			}

			@Override
			public String toString() {
				return "<builtin clock>";
			}
		});

		globals.define("str", new LoxCallable() {
			@Override
			public int arity() {
				return 1;
			}

			@Override
			public String call(Interpreter interpreter, List<Object> params) {
				return stringify(params.get(0));
			}

			@Override
			public String toString() {
				return "<builtin str>";
			}
		});
	}

	@Override
	public Object visitBinary(Expr.Binary expr) {
		// for logical operators, short-circuit execution
		Object left = evaluate(expr.left);
		TokenType opType = expr.operator.getType();
		if (opType == TokenType.AND || opType == TokenType.OR) {
			if (!isTruthy(left) && opType == TokenType.AND ||
							isTruthy(left) && opType == TokenType.OR) return left;
			return evaluate(expr.right);
		}

		// for assignment, do not evaluate the left-hand side
		if (opType == TokenType.EQUAL) {
				Object value = evaluate(expr.right);
				environment.assign(((Expr.Variable) expr.left).name, value);
				return value;
		}
		
		Object right = evaluate(expr.right);

		switch (opType) {
			case PLUS:
				if (left instanceof Double && right instanceof Double)
					return (double) left + (double) right;
				if (left instanceof String && right instanceof String)
					return (String) left + (String) right;

				throw error(expr.operator, "Operands must be either both strings or both numbers.");
		}

		switch (opType) {
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);
		}

		checkNumberOperands(expr.operator, left, right);
		switch (opType) {
			case MINUS:
				return (double) left - (double) right;
			case SLASH:
				return (double) left / (double) right;
			case STAR:
				return (double) left * (double) right;
			case GREATER:
				return (double) left > (double) right;
			case GREATER_EQUAL:
				return (double) left >= (double) right;
			case LESS:
				return (double) left < (double) right;
			case LESS_EQUAL:
				return (double) left <= (double) right;
		}

		throw new AssertionError(opType);
	}

	private boolean isEqual(Object left, Object right) {
		return Objects.equals(left, right);
	}

	private boolean isTruthy(Object val) {
		if (val == null) return false;
		if (val instanceof Boolean) return (boolean) val;
		return true;
	}

	@Override
	public Object visitUnary(Expr.Unary expr) {
		Object val = expr.expr.accept(this);
		switch (expr.operator.getType()) {
			case MINUS:
				return -checkNumberOperand(expr.operator, val);
			case BANG:
				return !isTruthy(val);
		}
		throw new AssertionError();
	}

	@Override
	public Object visitLiteral(Expr.Literal expr) {
		return expr.value;
	}

	@Override
	public Object visitVariable(Expr.Variable expr) {
		return environment.get(expr.name);
	}

	@Override
	public Object visitGrouping(Expr.Grouping expr) {
		return expr.expr.accept(this);
	}

	@Override
	public Object visitCall(Expr.Call expr) {
		Object callee = evaluate(expr.callee);
		if (!(callee instanceof LoxCallable)) {
			throw error(expr.paren, "Can only call functions and classes.");
		}

		LoxCallable callable = (LoxCallable) callee;
		if (callable.arity() != expr.arguments.size()) {
			throw error(expr.paren,
			            String.format("Wrong number of arguments, when calling %s. " +
							                          "Expected %s, was %s",
			                          callable, callable.arity(), expr.arguments.size()));
		}

		List<Object> argValues = new LinkedList<>();
		for (Expr argument : expr.arguments) {
			argValues.add(evaluate(argument));
		}

		return callable.call(this, argValues);
	}

	@Override
	public Void visitExpression(Stmt.Expression stmt) {
		evaluate(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrint(Stmt.Print stmt) {
		System.out.println(stringify(evaluate(stmt.expression)));
		return null;
	}

	@Override
	public Void visitBlock(Stmt.Block stmt) {
		executeBlock(stmt.stmts, new Environment(this.environment));
		return null;
	}

	@Override
	public Void visitVar(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}

		environment.define(stmt.name.getLexeme(), value);
		return null;
	}

	@Override
	public Void visitFunDecl(Stmt.FunDecl stmt) {
		globals.define(stmt.name.getLexeme(),
		               new LoxFunction(stmt, new Environment(this.environment)));
		return null;
	}

	@Override
	public Void visitReturn(Stmt.Return stmt) {
		Object val = null;
		if (stmt.value != null) {
			val = evaluate(stmt.value);
		}
		throw new Return(val);
	}

	@Override
	public Void visitIf(Stmt.If stmt) {
		if (isTruthy(evaluate(stmt.cond))) {
			execute(stmt.thenBranch);
		} else if (stmt.elseBranch != null) {
			execute(stmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitWhile(Stmt.While stmt) {
		while (isTruthy(evaluate(stmt.cond))) {
			execute(stmt.body);
		}
		return null;
	}

	public void interpret(List<Stmt> stmts) {
		for (Stmt stmt : stmts) {
			execute(stmt);
		}
	}

	private void execute(Stmt stmt) {
		stmt.accept(this);
	}
	
	private String stringify(Object input) {
		if (input == null) return "nil";

		if (input instanceof Double) {
			double v = (double) input;
			if (v % 1 == 0) {
				return String.format("%.0f", v);
			}
		}

		return String.valueOf(input);
	}
	
	private Object evaluate(Expr expr) {
		return expr.accept(this);
	}

	private double checkNumberOperand(Token operator, Object runtimeValue) {
		if (!(runtimeValue instanceof Double))
			throw error(operator, "Operand must be a number.");
		return (double) runtimeValue;
	}

	private void checkNumberOperands(Token operator, Object left, Object right) {
		if (!(left instanceof Double))
			throw error(operator, String.format("First operand of %s must be a number.", operator.getLexeme()));
		if (!(right instanceof Double))
			throw error(operator, String.format("Second operand of %s must be a number.", operator.getLexeme()));
	}

	private RuntimeError error(Token operator, String msg) {
		return new RuntimeError(operator, msg);
	}

	public void executeBlock(List<Stmt> body, Environment callEnv) {
		Environment parentEnvironment = this.environment;
		try {
			this.environment = callEnv;
			for (Stmt child : body) {
				execute(child);
			}
		} finally {
			this.environment = parentEnvironment;
		}
	}

}
