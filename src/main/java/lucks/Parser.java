package lucks;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.*;

import static java.util.Arrays.asList;
import static lucks.TokenType.*;

public class Parser {

	private static final Map<TokenType, Integer> priorities = new HashMap<>();
	private static final Map<TokenType, Boolean> leftAssoc = new HashMap<>();
	public static final int MAX_FUN_ARGS = 8;

	static {
		TokenType[][] pdef = {
						{EQUAL},
						{OR},
						{AND},
						{EQUAL_EQUAL, BANG_EQUAL},
						{LESS, LESS_EQUAL, GREATER_EQUAL, GREATER},
						{MINUS, PLUS},
						{STAR, SLASH},
						{DOT}
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
		List<Stmt> program = new ArrayList<>();
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
			if (match(VAR)) return varDeclaration();
			else if (match(FUN)) return funDeclaration("function");
			else if (match(CLASS)) return classDeclaration();
			else return statement();
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

	private Stmt classDeclaration() {
		Token className = consume(IDENTIFIER, " class name expected");

		Token superClass = null;
		if (match(TokenType.LESS)) {
			superClass = consume(TokenType.IDENTIFIER);
		}
		consume(LEFT_BRACE, " before class body");

		List<Stmt.FunDecl> methods = new LinkedList<>();
		while (!match(RIGHT_BRACE)) {
			methods.add(funDeclaration("method"));
		}

		return new Stmt.Class(className, superClass, methods);
	}

	private Stmt.FunDecl funDeclaration(String kind) {
		Token name = consume(IDENTIFIER, " for " + kind);

		consume(LEFT_PAREN);
		List<Token> params = new LinkedList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				params.add(consume(IDENTIFIER));
			} while (match(COMMA));
		}
		Token paren = consume(RIGHT_PAREN);

		if (params.size() > MAX_FUN_ARGS) {
			error(paren, String.format(
							"functions are limited to at most %s arguments, was %d",
							MAX_FUN_ARGS, params.size()));
		}

		consume(LEFT_BRACE);
		List<Stmt> body = block();
		return new Stmt.FunDecl(name, params, body);
	}

	private Stmt statement() {
		if (match(PRINT)) return parsePrintStmt();
		if (match(RETURN)) return parseReturnStmt();
		if (match(IF)) return parseIfStmt();
		if (match(WHILE)) return parseWhileStmt();
		if (match(FOR)) return parseForStmt();
		else if (match(LEFT_BRACE)) return parseBlock();
		else return parseExprStmt();
	}

	private Stmt parseBlock() {
		return new Stmt.Block(block());
	}

	private List<Stmt> block() {
		List<Stmt> stmts = new ArrayList<>();
		while (!isAtEnd() && !check(RIGHT_BRACE)) {
			stmts.add(declaration());
		}
		consume(RIGHT_BRACE);
		return stmts;
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

	private Stmt parseReturnStmt() {
		Token keyword = previous();
		Expr expr = null;
		if (!check(SEMICOLON)) {
			expr = expression();
		}
		consume(SEMICOLON);
		return new Stmt.Return(keyword, expr);
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

	private Stmt parseWhileStmt() {
		consume(LEFT_PAREN);
		Expr cond = expression();
		consume(RIGHT_PAREN);

		Stmt body = statement();

		return new Stmt.While(cond, body);
	}

	private Stmt parseForStmt() {
		consume(LEFT_PAREN);

		Stmt init;
		if (match(SEMICOLON)) {
			init = null;
		} else if (match(VAR)) {
			init = varDeclaration();
		} else {
			init = parseExprStmt();
		}
		
		Expr cond = null;
		if (!check(SEMICOLON)) {
			cond = expression();
		}
		consume(SEMICOLON);

		Expr incr = null;
		if (!check(RIGHT_PAREN)) {
			incr = expression();
		}
		consume(RIGHT_PAREN);

		Stmt body = statement();

		if (incr != null) {
			body = new Stmt.Block(asList(body, new Stmt.Expression(incr)));
		}

		if (cond == null) cond = new Expr.Literal(true);
		Stmt.While loop = new Stmt.While(cond, body);

		if (init != null) {
			return new Stmt.Block(asList(init, loop));
		} else {
			return loop;
		}
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
				if (!(expr instanceof Expr.Variable) &&
								!(expr instanceof Expr.Binary && ((Expr.Binary) expr).right instanceof Expr.Variable)) {
					throw error(peek(), "Left hand side of assignment must be a l-value.");
				}
			}

			Token operator = advance();
			Expr rhs = expression(nextPrio);
			if (opType == EQUAL && expr instanceof Expr.Binary && ((Expr.Binary) expr).right instanceof Expr.Variable) {
				Expr.Binary lhs = (Expr.Binary) expr;
				expr = new Expr.Set(lhs.left, ((Expr.Variable) lhs.right).name, rhs);
			} else {
				expr = new Expr.Binary(expr, operator, rhs);
			}
		}
		
		return expr;
	}

	private Expr unary() {
		if (match(BANG, MINUS)) return new Expr.Unary(previous(), unary());
		return call();
	}

	private Expr call() {
		Expr expr = primary();

		while (match(LEFT_PAREN)) {
			expr = finishCall(expr);
		}
		
		return expr;
	}

	private Expr finishCall(Expr expr) {
		List<Expr> args = new ArrayList<>();
		if (!check(RIGHT_PAREN)) {
			do {
				args.add(expression());
			} while (match(COMMA));
		}
		Token paren = consume(RIGHT_PAREN);

		if (args.size() > MAX_FUN_ARGS) {
			error(paren, String.format(
							"functions are limited to at most %s arguments, was %d",
							MAX_FUN_ARGS, args.size()));
		}

		return new Expr.Call(expr, paren, args);
	}

	private Expr primary() {
		if (match(TRUE)) return new Expr.Literal(true);
		if (match(FALSE)) return new Expr.Literal(false);
		if (match(NIL)) return new Expr.Literal(null);
		if (match(THIS)) return new Expr.This(previous());

		if (match(NUMBER, STRING)) {
			return new Expr.Literal(previous().getLiteral());
		}

		if (match(LEFT_PAREN)) {
			Expr expr = expression(0);
			consume(RIGHT_PAREN);
			return new Expr.Grouping(expr);
		}

		if (match(SUPER)) {
			Token keyword = previous();
			consume(DOT);
			Token name = consume(IDENTIFIER);
			return new Expr.Super(keyword, name);
		}

		if (match(IDENTIFIER)) {
			return new Expr.Variable(previous());
		}

		throw error(peek(), "Expression expected.");
	}

	private Token consume(TokenType tokenType) {
		return consume(tokenType, "");
	}

	private Token consume(TokenType tokenType, String s) {
		if (match(tokenType)) {
			return previous();
		} else {
			throw error(peek(), String.format("expected %s, but was %s" + s,
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
