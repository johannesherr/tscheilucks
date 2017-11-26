package lucks;

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


	public interface Visitor<T> {
		T visitBinary(Expr.Binary expr);
		T visitUnary(Expr.Unary expr);
		T visitLiteral(Expr.Literal expr);
		T visitVariable(Expr.Variable expr);
		T visitGrouping(Expr.Grouping expr);
	}
}
