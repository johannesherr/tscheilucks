package lucks;

import static lucks.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Parser {

	private static final Map<TokenType, Integer> priorities = new HashMap<>();
	private static final Map<TokenType, Boolean> leftAssoc = new HashMap<>();
	static {
		TokenType[][] pdef = {
						{EQUAL},
						{OR},
						{AND},
						{EQUAL_EQUAL, BANG_EQUAL},
						{LESS, LESS_EQUAL, GREATER_EQUAL, GREATER},
						{MINUS, PLUS},
						{STAR, SLASH},
						// grouping
		};

		ArrayList<Object> operators = Lists.newArrayList();
		for (int i = 0; i < pdef.length; i++) {
			for (TokenType type : pdef[i]) {
				priorities.put(type, i + 1);
				operators.add(type);
				leftAssoc.put(type, true);
			}
		}
		leftAssoc.put(EQUAL, false);

		ops = operators.toArray(new TokenType[operators.size()]);
	}

	private final List<Token> tokens;
	private int current;
	private static TokenType[] ops;

	public Parser(List<Token> tokens) {
		this.tokens = tokens;
	}

	public List<Stmt> parse() {
		List<Stmt> program = new LinkedList<>();
		while (!isAtEnd()) {
			Stmt stmt = declaration();
			if (stmt != null) {
				program.add(stmt);
			}
		}
		return program;
	}

	private Stmt declaration() {
		try {
			if (match(VAR)) {
				return varDeclaration();
			} else {
				return statement();
			}
		} catch (ParseError error) {
			synchronize();
			return null;
		}
	}

	private Stmt varDeclaration() {
		Token name = consume(IDENTIFIER);

		Expr initializer = null;
		if (match(EQUAL)) {
			initializer = expression();
		}

		consume(SEMICOLON);
		return new Stmt.Var(name, initializer);
	}

	private Stmt statement() {
		if (match(PRINT)) return parsePrintStmt();
		if (match(IF)) return parseIfStmt();
		else if (match(LEFT_BRACE)) return parseBlock();
		else return parseExprStmt();
	}

	private Stmt parseBlock() {
		List<Stmt> stmts = new LinkedList<>();
		while (!isAtEnd() && !check(RIGHT_BRACE)) {
			stmts.add(declaration());
		}
		consume(RIGHT_BRACE);
		return new Stmt.Block(stmts);
	}

	private Stmt.Expression parseExprStmt() {
		Expr expr = expression();
		consume(SEMICOLON);
		return new Stmt.Expression(expr);
	}

	private Stmt parsePrintStmt() {
		Expr expr = expression();
		consume(SEMICOLON);
		return new Stmt.Print(expr);
	}

	private Stmt parseIfStmt() {
		consume(LEFT_PAREN);
		Expr cond = expression();
		consume(RIGHT_PAREN);

		Stmt ifCase = statement();
		Stmt elseCase = null;
		if (match(ELSE)) {
			elseCase = statement();
		}

		return new Stmt.If(cond, ifCase, elseCase);
	}

	private Expr expression() {
		return expression(0);
	}

	private Expr expression(int priority) {
		Expr expr = unary();

		while (peekType(ops)) {
			TokenType opType = peek().getType();
			Integer nextPrio = priorities.get(opType);
			if (nextPrio < priority || (nextPrio == priority && leftAssoc.get(opType)))
				break;

			if (opType == EQUAL) {
				if (!(expr instanceof Expr.Variable)) {
					throw error(peek(), "Left hand side of assignment must be a l-value.");
				}
			}

			expr = new Expr.Binary(expr, advance(), expression(nextPrio));
		}

		return expr;
	}

	private Expr unary() {
		if (match(BANG, MINUS)) return new Expr.Unary(previous(), unary());
		return primary();
	}

	private Expr primary() {
		if (match(TRUE)) return new Expr.Literal(true);
		if (match(FALSE)) return new Expr.Literal(false);
		if (match(NIL)) return new Expr.Literal(null);

		if (match(NUMBER, STRING)) {
			return new Expr.Literal(previous().getLiteral());
		}

		if (match(LEFT_PAREN)) {
			Expr expr = expression(0);
			consume(RIGHT_PAREN);
			return new Expr.Grouping(expr);
		}

		if (match(IDENTIFIER)) {
			return new Expr.Variable(previous());
		}

		throw error(peek(), "Expression expected.");
	}

	private Token consume(TokenType tokenType) {
		if (match(tokenType)) {
			return previous();
		} else {
			throw error(peek(), String.format("expected %s, but was %s",
			                                  tokenType,
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
		return current == tokens.size() - 1;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	private void synchronize() {
		HashSet<TokenType> target = Sets.newHashSet(
						CLASS,
						FUN,
						VAR,
						FOR,
						IF,
						WHILE,
						PRINT,
						RETURN
		);
		while (!isAtEnd() && (previous().getType() != SEMICOLON || !target.contains(peek().getType()))) {
			advance();
		}
	}
}
