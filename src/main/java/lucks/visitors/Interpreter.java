package lucks.visitors;

import java.util.List;
import java.util.Objects;

import lucks.Environment;
import lucks.Expr;
import lucks.RuntimeError;
import lucks.Stmt;
import lucks.Token;
import lucks.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

	private final Environment environment = new Environment();

	@Override
	public Object visitBinary(Expr.Binary expr) {
		// for assignment, do not evaluate the left-hand side
		if (expr.operator.getType() == TokenType.EQUAL) {
				Object value = expr.right.accept(this);
				environment.assign(((Expr.Variable) expr.left).name, value);
				return value;
		}
		
		Object left = expr.left.accept(this);
		Object right = expr.right.accept(this);

		TokenType opType = expr.operator.getType();
		switch (opType) {
			case PLUS:
				if (left instanceof Double && right instanceof Double)
					return (double) left + (double) right;
				if (left instanceof String && right instanceof String)
					return (String) left + (String) right;

				throw error(expr.operator, "Operands must be either both strings or both numbers.");
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

		switch (opType) {
			case BANG_EQUAL:
				return !isEqual(left, right);
			case EQUAL_EQUAL:
				return isEqual(left, right);


//			case AND:
//				return isTruthy(left) && isTruthy(right);
//			case OR:
//				return isTruthy(left) || isTruthy(right);
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
	public Void visitVar(Stmt.Var stmt) {
		Object value = null;
		if (stmt.initializer != null) {
			value = evaluate(stmt.initializer);
		}

		environment.define(stmt.name.getLexeme(), value);
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
			throw error(operator, "Operand must be a number.");
		if (!(right instanceof Double))
			throw error(operator, "Operand must be a number.");
	}

	private RuntimeError error(Token operator, String msg) {
		return new RuntimeError(operator, msg);
	}
}
