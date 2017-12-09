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
import lucks.visitors.Resolver;

/**
 * @author Johannes Herr
 */
public class Lox {

	private static final Interpreter interpreter = new Interpreter();
	private static final String RUN_LATEST = "run-latest";
	private static final String RUN_ALL = "run-all";
	private static boolean hadError;
	private static boolean hadRuntimeError;

	public static void main(String[] args) throws IOException {
		Path[] scripts = Files.list(Paths.get("."))
				.filter(p -> p.getFileName().toString().matches("script\\d.txt"))
				.sorted()
				.toArray(Path[]::new);

		String mode = RUN_LATEST;
//		String mode = RUN_ALL;

		if (mode.equals(RUN_LATEST)) {
			args = new String[]{scripts[scripts.length - 1].toString()};
		} else if (mode.equals(RUN_ALL)) {
			for (Path script : scripts) {
				System.out.printf("script = %s%n", script);
				runFile(script);
			}			
		}

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
		try {
			run(content);
		} catch (RuntimeError e) {
			runtimeError(e);
		}
		if (hadError) System.exit(65);
		if (hadRuntimeError) System.exit(70);
	}

	private static void runPrompt() throws IOException {
		BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			try {
				System.out.print("> ");
				run(rdr.readLine());
			} catch (RuntimeError e) {
				runtimeError(e);
			}
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

		Resolver resolver = new Resolver(interpreter);
		resolver.resolveBlock(stmts);
		if (hadError) return;

		hadError = false;
		interpreter.interpret(stmts);
	}

	static void error(int line, String msg) {
		report(line, "", msg);
	}
	
	public static void error(Token token, String msg) {
		String location = token.getType() == TokenType.EOF ? " at end" : " at '" + token.getLexeme() + "'";
		report(token.getLine(), location, msg);
	}
	
	private static void report(int line, String location, String msg) {
		System.err.println(String.format("[line %,d] Error%s: %s", line, location, msg));
		hadError = true;
	}

	public static void runtimeError(RuntimeError error) {
		System.err.println(String.format("%s\n[line %d]", error.getMessage(), error.token.getLine()));
		hadRuntimeError = true;
	}
}
