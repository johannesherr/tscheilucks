package lucks;

public class RuntimeError extends RuntimeException {

	public final Token token;

	public RuntimeError(Token token, String msg) {
		super(msg);
		this.token = token;
	}
}
