package lucks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Johannes Herr
 */
public class Lox {

	private static boolean hadError;

	public static void main(String[] args) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: jlox [script]");
		} else if (args.length == 1) {
			runFile(Paths.get(args[0]));
		} else {
			runPrompt();
		}
	}

	private static void runFile(Path path) throws IOException {
		String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
		run(content);
		if (hadError) System.exit(65);
	}

	private static void runPrompt() throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.print("> ");
			run(rdr.readLine());
			hadError = false;
		}
	}
	
	private static void run(String content) {
		Scanner sc = new Scanner(content);
		List<Token> tokens = sc.scanTokens();
		Parser parser = new Parser(tokens);

		Expr expr = parser.parse();
		if (hadError) return;

		System.out.println(expr.accept(new AstPrinter()));
	}
	
	static void error(int line, String msg) {
		report(line, "", msg);
	}

	static void error(Token token, String msg) {
		String details = token.getType() == TokenType.EOF ? " at end" : " at '" + token.getLexeme() + "'";
		report(token.getLine(), details, msg);
	}
	
	private static void report(int line, String where, String msg) {
		System.err.println(String.format("[line %,d] Error%s: %s", line, where, msg));
		hadError = true;
	}
}
