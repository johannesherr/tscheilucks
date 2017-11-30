package lucks;

import java.util.List;

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

	public static class Block extends Stmt {
		public final List<Stmt> stmts;

		public Block(List<Stmt> stmts) {
			this.stmts = stmts;
		}

		public <T> T accept(Stmt.Visitor<T> visitor) {
			return visitor.visitBlock(this);
		}

		@Override
		public String toString() {
			return "Block{" + "stmts=" + stmts + "}";
		}
	}

	public static class Var extends Stmt {
		public final Token name;
		public final Expr initializer;

		public Var(Token name, Expr initializer) {
			this.name = name;
			this.initializer = initializer;
		}

		public <T> T accept(Stmt.Visitor<T> visitor) {
			return visitor.visitVar(this);
		}

		@Override
		public String toString() {
			return "Var{" + "name=" + name + ", " + "initializer=" + initializer + "}";
		}
	}

	public static class If extends Stmt {
		public final Expr cond;
		public final Stmt thenBranch;
		public final Stmt elseBranch;

		public If(Expr cond, Stmt thenBranch, Stmt elseBranch) {
			this.cond = cond;
			this.thenBranch = thenBranch;
			this.elseBranch = elseBranch;
		}

		public <T> T accept(Stmt.Visitor<T> visitor) {
			return visitor.visitIf(this);
		}

		@Override
		public String toString() {
			return "If{" + "cond=" + cond + ", " + "thenBranch=" + thenBranch + ", " + "elseBranch=" + elseBranch + "}";
		}
	}


	public interface Visitor<T> {
		T visitExpression(Stmt.Expression stmt);
		T visitPrint(Stmt.Print stmt);
		T visitBlock(Stmt.Block stmt);
		T visitVar(Stmt.Var stmt);
		T visitIf(Stmt.If stmt);
	}
}
