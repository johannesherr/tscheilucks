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

	// 21:28-
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

		for (Token token : tokens) {
			System.out.println(token);
		}
	}

	static void error(int line, String msg) {
		report(line, "", msg);
	}

	private static void report(int line, String where, String msg) {
		System.err.println(String.format("[line %,d] Error%s: %s", line, where, msg));
		hadError = true;
	}
}
