package lucks;

public abstract class Stmt {
	
	public abstract <T> T accept(Stmt.Visitor<T> visitor);

	public static class Expression extends Stmt {
		public final Expr expression;

		public Expression(Expr expression) {
			this.expression = expression;
		}

		public <T> T accept(Stmt.Visitor<T> visitor) {
			return visitor.visitExpression(this);
		}

		@Override
		public String toString() {
			return "Expression{" + "expression=" + expression + "}";
		}
	}

	public static class Print extends Stmt {
		public final Expr expression;

		public Print(Expr expression) {
			this.expression = expression;
		}

		public <T> T accept(Stmt.Visitor<T> visitor) {
			return visitor.visitPrint(this);
		}

		@Override
		public String toString() {
			return "Print{" + "expression=" + expression + "}";
		}
	}


	public interface Visitor<T> {
		T visitExpression(Stmt.Expression stmt);
		T visitPrint(Stmt.Print stmt);
	}
}
