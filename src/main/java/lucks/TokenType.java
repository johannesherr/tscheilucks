package lucks;

/**
 * @author Johannes Herr
 */
public enum TokenType {
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

	IDENTIFIER, STRING, NUMBER,

	IF, ELSE, WHILE, FOR, FUN, CLASS, NIL, AND, OR, PRINT, RETURN, SUPER, THIS, TRUE, FALSE, VAR,

	EOF

}
