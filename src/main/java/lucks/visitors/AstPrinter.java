package lucks.visitors;

import java.util.stream.Collectors;

import lucks.Expr;
import lucks.Stmt;


/**
 * @author Johannes Herr
 */
public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
	@Override
	public String visitBinary(Expr.Binary expr) {
		return String.format("(%s %s %s)", expr.operator.getLexeme(),
		                     expr.left.accept(new AstPrinter()),
		                     expr.right.accept(new AstPrinter()));
	}

	@Override
	public String visitUnary(Expr.Unary expr) {
		return String.format("(%s %s)", expr.operator.getLexeme(), expr.expr.accept(new AstPrinter()));
	}

	@Override
	public String visitLiteral(Expr.Literal expr) {
		return expr.value.toString();
	}

	@Override
	public String visitVariable(Expr.Variable expr) {
		return expr.name.getLexeme();
	}

	@Override
	public String visitGrouping(Expr.Grouping expr) {
		return String.format("(group %s)", expr.expr.accept(new AstPrinter()));
	}

	public static String print(Expr expression) {
		return expression.accept(new AstPrinter());
	}

	@Override
	public String visitExpression(Stmt.Expression stmt) {
		return stmt.expression.accept(this);
	}

	@Override
	public String visitPrint(Stmt.Print stmt) {
		return String.format("(print %s)", stmt.expression.accept(this));
	}

	@Override
	public String visitBlock(Stmt.Block stmt) {
		return String.format("(%s)", stmt.stmts.stream().map(sm -> sm.accept(this)).collect(Collectors.joining(", ")));
	}

	@Override
	public String visitVar(Stmt.Var stmt) {
		return String.format("(def %s %s)", stmt.name.getLexeme(), stmt.initializer.accept(this));
	}

	@Override
	public String visitIf(Stmt.If stmt) {
		return String.format("(if %s %s %s)",
		                     stmt.cond.accept(this),
						             stmt.thenBranch.accept(this),
						             stmt.elseBranch.accept(this));
	}

	@Override
	public String visitWhile(Stmt.While stmt) {
		return String.format("(while %s %s)",
		                     stmt.cond.accept(this),
		                     stmt.body.accept(this));
	}
}
