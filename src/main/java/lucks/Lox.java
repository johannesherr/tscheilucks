package lucks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lucks.visitors.Interpreter;

/**
 * @author Johannes Herr
 */
public class Lox {

	private static final Interpreter interpreter = new Interpreter();
	private static boolean hadError;
	private static boolean hadRuntimeError;

	public static void main(String[] args) throws IOException {
		args = new String[]{"script.txt"};
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
		if (hadRuntimeError) System.exit(70);
	}

	private static void runPrompt() throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.print("> ");
			run(rdr.readLine());
		}
	}
	
	private static void run(String content) {
		hadError = false;
		Scanner sc = new Scanner(content);
		List<Token> tokens = sc.scanTokens();
		if (hadError) return;

		hadError = false;
		Parser parser = new Parser(tokens);
		List<Stmt> stmts = parser.parse();
		if (hadError) return;

		hadError = false;
		interpreter.interpret(stmts);
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

	public static void runtimeError(RuntimeError error) {
		System.err.println(String.format("%s\n[line %d]", error.getMessage(), error.token.getLine()));
		hadRuntimeError = true;
	}
}
