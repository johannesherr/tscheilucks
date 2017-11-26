package lucks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Johannes Herr
 */
public enum TokenType {
	LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

	BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

	IDENTIFIER, STRING, NUMBER,

	IF, ELSE, WHILE, FOR, FUN, CLASS, NIL, AND, OR, PRINT, RETURN, SUPER, THIS, TRUE, FALSE, VAR,

	EOF;

	// 13:59-
	public static void main(String[] args) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		List<String> choose = new LinkedList<>();
		for (TokenType tokenType : TokenType.values()) {
			System.out.println(tokenType);
			String c = in.readLine();
			if (c.trim().equals("1")) {
				choose.add(tokenType + "");
			}
		}

		System.out.println(choose.stream().map(n -> "TokenType." + n).collect(Collectors.joining(",\n")));
	}
}
