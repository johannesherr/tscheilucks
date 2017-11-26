package lucks;

public class ParseError extends RuntimeException {

	private final Token token;

	public ParseError(Token token, String msg) {
		super(msg);
		this.token = token;
	}

	public int getLine() {
		return token.getLine();
	}
}
