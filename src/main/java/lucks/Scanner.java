package lucks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * @author Johannes Herr
 */
public class Scanner {

	private final List<Token> tokens = new LinkedList<>();
	private final String content;
	private int pos = 0;
	private int line = 1;

	private static final Map<String, TokenType> map = new HashMap<>();
	static {
		map.put("if", TokenType.IF);
		map.put("else", TokenType.ELSE);
		map.put("while", TokenType.WHILE);
		map.put("for", TokenType.FOR);
		map.put("fun", TokenType.FUN);
		map.put("class", TokenType.CLASS);
		map.put("nil", TokenType.NIL);
		map.put("and", TokenType.AND);
		map.put("or", TokenType.OR);
		map.put("print", TokenType.PRINT);
		map.put("return", TokenType.RETURN);
		map.put("super", TokenType.SUPER);
		map.put("this", TokenType.THIS);
		map.put("true", TokenType.TRUE);
		map.put("false", TokenType.FALSE);
		map.put("var", TokenType.VAR);
	}

	public Scanner(String content) {
		this.content = content;
	}

	// 21:54-
	public static void main(String[] args) {
		String[] split = "{ } ( ) , . - + ; / *".split(" ");
		String[] dbl = "! = > <".split(" ");
		TokenType[] values = TokenType.values();
		for (int i = split.length + dbl.length * 2; i < values.length; i++) {
			TokenType tokenType = values[i];
			System.out.println(String.format("\tmap.put(\"%s\", TokenType.%s);", tokenType.toString().toLowerCase(), tokenType));
		}
	}

	public List<Token> scanTokens() {

		while (!isAtEOF()) {
			char c = peek();
			switch (c) {
				case '(': addToken(TokenType.LEFT_PAREN); break;
				case ')': addToken(TokenType.RIGHT_PAREN); break;
				case '{': addToken(TokenType.LEFT_BRACE); break;
				case '}': addToken(TokenType.RIGHT_BRACE); break;
				case ',': addToken(TokenType.COMMA); break;
				case '.': addToken(TokenType.DOT); break;
				case '-': addToken(TokenType.MINUS); break;
				case '+': addToken(TokenType.PLUS); break;
				case ';': addToken(TokenType.SEMICOLON); break;
				case '*': addToken(TokenType.STAR); break;

				case '!': addDblToken(TokenType.BANG, '=', TokenType.BANG_EQUAL); break;
				case '=': addDblToken(TokenType.EQUAL, '=', TokenType.EQUAL_EQUAL); break;
				case '>': addDblToken(TokenType.GREATER, '=', TokenType.GREATER_EQUAL); break;
				case '<': addDblToken(TokenType.LESS, '=', TokenType.LESS_EQUAL); break;

				case '/':
					consume();
					if (match('/')) {
						while (!isAtEOF() && peek() != '\n') consume();
					} else {
						tokens.add(new Token(TokenType.SLASH, null, null, line));
					} 
					break;

				case ' ':
				case '\t':
				case '\r':
					consume();
					break;

				case '\n':
					consume();
					line++;
					break;

				case '"':
					scanString();
					break;

				default:
					if (isDigit(c)) {
						scanNumber();
					} else if (isAlpha(c)) {
						scanIdentifierOrKeyword();

					} else {
						consume();
						Lox.error(line, "Unexpected character: " + c);
					}

					break;
			}
		}

		tokens.add(new Token(TokenType.EOF, null, null, line));

		return tokens;
	}

	private void scanIdentifierOrKeyword() {
		int start = pos;

		while (!isAtEOF() && (isAlpha(peek()) || isDigit(peek()) || peek() == '_')) consume();

		String val = content.substring(start, pos);
		tokens.add(new Token(map.getOrDefault(val, TokenType.IDENTIFIER), val, null, line));
	}

	private boolean isAlpha(char c) {
		return 'A' <= c && c <= 'z';
	}

	private void scanString() {
		int start = this.pos;
		consume('"');
		while (!isAtEOF() && peek() != '"') {
			consume();
		}
		if (isAtEOF()) {
			Lox.error(line, "unclosed string literal");
			return;
		} else {
			consume('"');
		}
		String lit = content.substring(start, pos);
		tokens.add(new Token(TokenType.STRING, lit, lit.substring(1, lit.length() - 1), line));
	}

	private void scanNumber() {
		int start = this.pos;
		while (isDigit()) consume();
		if (match('.')) {
			consume();
			while (isDigit()) consume();
		}
		String lit = content.substring(start, pos);
		tokens.add(new Token(TokenType.NUMBER, lit, Double.parseDouble(lit), line));
	}

	private boolean isDigit() {
		return !isAtEOF() && isDigit(peek());
	}

	private boolean isDigit(char c) {
		return '0' <= c && c <= '9';
	}

	private void addToken(TokenType type) {
		char c = consume();
		tokens.add(new Token(type, String.valueOf(c), null, line));
	}

	private void addDblToken(TokenType simple, char next, TokenType dbl) {
		char c1 = consume();
		Token token;
		if (match(next)) {
			char c2 = consume();
			token = new Token(dbl, c1 + "" + c2, null, line);
		} else {
			token = new Token(simple, String.valueOf(c1), null, line);
		}
		tokens.add(token);
	}

	private boolean match(char next) {
		return !isAtEOF() && peek() == next;
	}

	private char consume() {
		return content.charAt(pos++);
	}

	private char consume(char exp) {
		char c = consume();
		if (c != exp) Lox.error(line, String.format("expected '%s', but was '%s'", exp, c));
		return c;
	}

	private char peek() {
		return content.charAt(pos);
	}

	private boolean isAtEOF() {
		return pos >= content.length();
	}
}
