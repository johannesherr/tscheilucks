package lucks.visitors;

import java.util.Objects;

import lucks.Expr;
import lucks.Lox;
import lucks.RuntimeError;
import lucks.Token;
import lucks.TokenType;

public class Interpreter implements Visitor<Object> {
	@Override
	public Object visitBinary(Expr.Binary expr) {
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
	public Object visitGrouping(Expr.Grouping expr) {
		return expr.expr.accept(this);
	}

	public void interpret(Expr expr) {
		try {
			System.out.println(stringify(evaluate(expr)));
		} catch (RuntimeError error) {
			Lox.runtimeError(error);
		}
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
