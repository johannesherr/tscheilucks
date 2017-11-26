package lucks.visitors;

import lucks.Expr;


/**
 * @author Johannes Herr
 */
public class AstPrinter implements Expr.Visitor<String> {
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
	public String visitGrouping(Expr.Grouping expr) {
		return String.format("(group %s)", expr.expr.accept(new AstPrinter()));
	}

	public static String print(Expr expression) {
		return expression.accept(new AstPrinter());
	}

}
