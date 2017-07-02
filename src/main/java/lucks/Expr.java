package lucks;

/**
 * @author Johannes Herr
 */
public abstract class Expr {
	
	public abstract <T> T accept(Visitor<T> visitor);

	public static class Binary extends Expr {
		final Expr left;
		final Token operator;
		final Expr right;

		public Binary(Expr left, Token operator, Expr right) {
			this.left = left;
			this.operator = operator;
			this.right = right;
		}

		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitBinary(this);
		}
	}

	public static class Unary extends Expr {
		final Token operator;
		final Expr expr;

		public Unary(Token operator, Expr expr) {
			this.operator = operator;
			this.expr = expr;
		}

		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitUnary(this);
		}
	}

	public static class Literal extends Expr {
		final Object value;

		public Literal(Object value) {
			this.value = value;
		}

		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitLiteral(this);
		}
	}

	public static class Grouping extends Expr {
		final Expr expr;

		public Grouping(Expr expr) {
			this.expr = expr;
		}

		public <T> T accept(Visitor<T> visitor) {
			return visitor.visitGrouping(this);
		}
	}

}
