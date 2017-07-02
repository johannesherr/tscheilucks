package lucks;

/**
 * @author Johannes Herr
 */
public class AstPrinter implements Visitor<String> {
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

	private String print(Expr expression) {
		return expression.accept(this);
	}

	// 18:14-
	public static void main(String[] args) {
		Expr expression = new Expr.Binary(
						new Expr.Unary(
										new Token(TokenType.MINUS, "-", null, 1),
										new Expr.Literal(123)),
						new Token(TokenType.STAR, "*", null, 1),
						new Expr.Grouping(
										new Expr.Literal(45.67)));

		System.out.println(new AstPrinter().print(expression));
	}
}
