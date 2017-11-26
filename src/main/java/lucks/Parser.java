package lucks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Parser {

	private static final Map<TokenType, Integer> priorities = new HashMap<>();
	static {
		TokenType[][] pdef = {
						{TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL},
						{TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER_EQUAL, TokenType.GREATER},
						{TokenType.MINUS, TokenType.PLUS},
						{TokenType.STAR, TokenType.SLASH},
						// grouping
		};

		ArrayList<Object> operators = Lists.newArrayList();
		for (int i = 0; i < pdef.length; i++) {
			for (TokenType type : pdef[i]) {
				priorities.put(type, i + 1);
				operators.add(type);
			}
		}

		ops = operators.toArray(new TokenType[operators.size()]);
	}

	private final List<Token> tokens;
	private int current;
	private static TokenType[] ops;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}


	public Expr parse() {
		try {
			return parseExpr(0);
		} catch (ParseError error) {
			return null;
		}
	}
	
	private Expr parseExpr(int priority) {
		Expr expr = unary();

		while (peekType(ops)) {
			Integer nextPrio = priorities.get(peek().getType());
			if (nextPrio <= priority) break;

			expr = new Expr.Binary(expr, advance(), parseExpr(nextPrio));
		}

		return expr;
	}

	private Expr unary() {
		if (match(TokenType.BANG, TokenType.MINUS)) return new Expr.Unary(previous(), unary());
		return primary();
	}

	private Expr primary() {
		if (match(TokenType.TRUE)) return new Expr.Literal(true);
		if (match(TokenType.FALSE)) return new Expr.Literal(false);
		if (match(TokenType.NIL)) return new Expr.Literal(null);

		if (match(TokenType.NUMBER, TokenType.STRING)) {
			return new Expr.Literal(previous().getLiteral());
		}

		if (match(TokenType.LEFT_BRACE)) {
			Expr expr = parseExpr(0);
			consume(TokenType.RIGHT_BRACE);
			return new Expr.Grouping(expr);
		}

		throw error(peek(), "Expression expected.");
	}

	private Token consume(TokenType rightBrace) {
		if (match(rightBrace)) {
			return previous();
		} else {
			throw error(peek(), String.format("expected %s, but was %s",
			                                  rightBrace,
			                                  peek().getType()));
		}
	}

	private ParseError error(Token token, String msg) {
		Lox.error(token, msg);
		return new ParseError(token, msg);
	}

	private boolean match(TokenType... types) {
		boolean ret = peekType(types);
		if (ret) advance();
		return ret;
	}

	private boolean peekType(TokenType... types) {
		for (TokenType type : types) {
			if (check(type)) {
				return true;
			}
		}

		return false;
	}

	private Token advance() {
		if (!isAtEnd()) current++;
		return previous();
	}

	private boolean check(TokenType type) {
		return !isAtEnd() && peek().getType() == type;
	}

	private boolean isAtEnd() {
		return current == tokens.size();
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	public void synchronize() {
		HashSet<TokenType> target = Sets.newHashSet(
						TokenType.CLASS,
						TokenType.FUN,
						TokenType.VAR,
						TokenType.FOR,
						TokenType.IF,
						TokenType.WHILE,
						TokenType.PRINT,
						TokenType.RETURN
		);
		while (!isAtEnd() && (previous().getType() != TokenType.SEMICOLON || !target.contains(peek().getType()))) {
			advance();
		}
	}
}
