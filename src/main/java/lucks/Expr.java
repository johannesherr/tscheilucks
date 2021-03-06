package lucks;

import java.util.List;

public abstract class Expr {
	
	public abstract <T> T accept(Expr.Visitor<T> visitor);

	public static class Binary extends Expr {
		public final Expr left;
		public final Token operator;
		public final Expr right;

		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitBinary(this);
		}

		@Override
		public String toString() {
			return "Binary{" + "left=" + left + ", " + "operator=" + operator + ", " + "right=" + right + "}";
		}
	}

	public static class Unary extends Expr {
		public final Token operator;
		public final Expr expr;

		public Unary(Token operator, Expr expr) {
			this.operator = operator;
			this.expr = expr;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitUnary(this);
		}

		@Override
		public String toString() {
			return "Unary{" + "operator=" + operator + ", " + "expr=" + expr + "}";
		}
	}

	public static class Literal extends Expr {
		public final Object value;

		public Literal(Object value) {
			this.value = value;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitLiteral(this);
		}

		@Override
		public String toString() {
			return "Literal{" + "value=" + value + "}";
		}
	}

	public static class Variable extends Expr {
		public final Token name;

		public Variable(Token name) {
			this.name = name;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitVariable(this);
		}

		@Override
		public String toString() {
			return "Variable{" + "name=" + name + "}";
		}
	}

	public static class Grouping extends Expr {
		public final Expr expr;

		public Grouping(Expr expr) {
			this.expr = expr;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitGrouping(this);
		}

		@Override
		public String toString() {
			return "Grouping{" + "expr=" + expr + "}";
		}
	}

	public static class Call extends Expr {
		public final Expr callee;
		public final Token paren;
		public final List<Expr> arguments;

		public Call(Expr callee, Token paren, List<Expr> arguments) {
			this.callee = callee;
			this.paren = paren;
			this.arguments = arguments;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitCall(this);
		}

		@Override
		public String toString() {
			return "Call{" + "callee=" + callee + ", " + "paren=" + paren + ", " + "arguments=" + arguments + "}";
		}
	}

	public static class Set extends Expr {
		public final Expr object;
		public final Token name;
		public final Expr value;

		public Set(Expr object, Token name, Expr value) {
			this.object = object;
			this.name = name;
			this.value = value;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitSet(this);
		}

		@Override
		public String toString() {
			return "Set{" + "object=" + object + ", " + "name=" + name + ", " + "value=" + value + "}";
		}
	}

	public static class This extends Expr {
		public final Token keyword;

		public This(Token keyword) {
			this.keyword = keyword;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitThis(this);
		}

		@Override
		public String toString() {
			return "This{" + "keyword=" + keyword + "}";
		}
	}

	public static class Super extends Expr {
		public final Token zuper;
		public final Token name;

		public Super(Token zuper, Token name) {
			this.zuper = zuper;
			this.name = name;
		}

		public <T> T accept(Expr.Visitor<T> visitor) {
			return visitor.visitSuper(this);
		}

		@Override
		public String toString() {
			return "Super{" + "zuper=" + zuper + ", " + "name=" + name + "}";
		}
	}


	public interface Visitor<T> {
		T visitBinary(Expr.Binary expr);
		T visitUnary(Expr.Unary expr);
		T visitLiteral(Expr.Literal expr);
		T visitVariable(Expr.Variable expr);
		T visitGrouping(Expr.Grouping expr);
		T visitCall(Expr.Call expr);
		T visitSet(Expr.Set expr);
		T visitThis(Expr.This expr);
		T visitSuper(Expr.Super expr);
	}
}
