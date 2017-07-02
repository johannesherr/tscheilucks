package lucks;

/**
 * @author Johannes Herr
 */
public interface Visitor<T> {
	T visitBinary(Expr.Binary expr);
	T visitUnary(Expr.Unary expr);
	T visitLiteral(Expr.Literal expr);
	T visitGrouping(Expr.Grouping expr);
}
