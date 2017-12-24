package lucks.tools;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * @author Johannes Herr
 */
public class GenAst {

	public static void main(String[] args) throws IOException {
		String clazzName = "Expr";
		List<Clazz> exprClasses = createAST(clazzName, asList(
						"Binary: Expr left, Token operator, Expr right",
						"Unary: Token operator, Expr expr",
						"Literal: Object value",
						"Variable: Token name",
						"Grouping: Expr expr",
						"Call: Expr callee, Token paren, List<Expr> arguments",
						"Set: Expr object, Token name, Expr value",
						"This: Token keyword"
		));

		List<Clazz> stmtClasses = createAST("Stmt", asList(
						"Expression: Expr expression",
						"Print: Expr expression",
						"Block: List<Stmt> stmts",
						"Var: Token name, Expr initializer",
						"FunDecl: Token name, List<Token> parameters, List<Stmt> body",
						"Return: Token keyword, Expr value",
						"If: Expr cond, Stmt thenBranch, Stmt elseBranch",
						"While: Expr cond, Stmt body",
						"Class: Token name, Token superClass, List<FunDecl> methods"
		));
	}

	private static List<Clazz> createAST(String clazzName, List<String> def) throws IOException {
		List<Clazz> exprClasses = parseDef(def);

		String clazzTemplate = "package lucks;\n" +
						"\n" +
						"import java.util.List;\n" +
						"\n" +
						"public abstract class %1$s {\n" +
						"\t\n" +
						"\tpublic abstract <T> T accept(%1$s.Visitor<T> visitor);\n" +
						"\n" +
						classes(exprClasses) +
						"\n" +
						"%2$s" +
						"}\n";

		String codeVisitor =
						"\n" +
						"\tpublic interface Visitor<T> {\n" +
						(exprClasses.stream()
										.map(c -> String.format("\t\tT visit%1$s(" + clazzName + ".%1$s " + clazzName.toLowerCase() + ");\n", c.name))
										.collect(Collectors.joining(""))) +
						"\t}\n";

		String code = String.format(clazzTemplate, clazzName, codeVisitor);

		Files.write(Paths.get("src/main/java/lucks/" + clazzName + ".java"), code.getBytes(StandardCharsets.UTF_8));
		return exprClasses;
	}

	private static class Field {
		String name;
		String type;

		public Field(String type, String name) {
			this.type = type;
			this.name = name;
		}
	}
	private static class Clazz {
		String name;
		List<Field> fields;

		public Clazz(String name, List<Field> fields) {
			this.name = name;
			this.fields = fields;
		}
	}

	private static String classes(List<Clazz> clazzes) {
		List<String> clsStrs = new LinkedList<>();
		for (Clazz clazz : clazzes) {
			clsStrs.add(
							String.format("\tpublic static class %1$s extends %%1$s {\n" +
											              (clazz.fields.stream()
															              .map(f -> String.format("\t\tpublic final %s %s;\n", f.type, f.name))
															              .collect(Collectors.joining(""))) +
											              "\n" +
											              "\t\tpublic %1$s(" + (clazz.fields.stream()
											.map(f -> f.type + " " + f.name)
											.collect(Collectors.joining(", "))) + ") {\n" +
											              (clazz.fields.stream()
															              .map(f -> String.format("\t\t\tthis.%s = %s;\n", f.name, f.name))
															              .collect(Collectors.joining(""))) +
											              "\t\t}\n" +
											              "\n" +
											              "\t\tpublic <T> T accept(%%1$s.Visitor<T> visitor) {\n" +
											              "\t\t\treturn visitor.visit%1$s(this);\n" +
											              "\t\t}\n", clazz.name) +

											(String.format("\n" +
															               "\t\t@Override\n" +
															               "\t\tpublic String toString() {\n" +
															               "\t\t\treturn \"%s{\" + %s + \"}\";\n" +
															               "\t\t}\n",
											               clazz.name,
											               clazz.fields.stream()
															               .map(f -> String.format("\"%s=\" + %s", f.name, f.name))
															               .collect(Collectors.joining(" + \", \" + "))))

			        + "\t}\n"
			);
		}

		return Joiner.on("\n").join(clsStrs);
	}

	private static List<Clazz> parseDef(List<String> def) {
		List<Clazz> dd = new LinkedList<>();
		for (String d : def) {
			String[] p1 = d.split("\\s*:\\s*");
			String name = p1[0];
			String[] p2 = p1[1].split("\\s*,\\s*");
			List<Field> fields = new LinkedList<>();
			for (String fieldDef : p2) {
				String[] p3 = fieldDef.split("\\s+");
				fields.add(new Field(p3[0], p3[1]));
			}
			dd.add(new Clazz(name, fields));
		}
		return dd;
	}
}
