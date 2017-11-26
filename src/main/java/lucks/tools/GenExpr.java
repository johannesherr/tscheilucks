package lucks.tools;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;

/**
 * @author Johannes Herr
 */
public class GenExpr {

	// 17:38-
	public static void main(String[] args) throws IOException {
		List<String> def = asList(
						"Binary: Expr left, Token operator, Expr right",
						"Unary: Token operator, Expr expr",
						"Literal: Object value",
						"Grouping: Expr expr"
		);
		List<Clazz> clazzes = parseDef(def);

		String code = "package lucks;\n" +
						"\n" +
						"import lucks.visitors.Visitor;\n" +
						"\n" +
						"/**\n" +
						" * @author Johannes Herr\n" +
						" */\n" +
						"public abstract class Expr {\n" +
						"\t\n" +
						"\tpublic abstract <T> T accept(Visitor<T> visitor);\n" +
						"\n" +
						classes(clazzes) +
						"\n" +
						"}\n";

		Files.write(Paths.get("src/main/java/lucks/Expr.java"), code.getBytes(StandardCharsets.UTF_8));

		String codeVisitor = "package lucks.visitors;\n" +
						"\n" +
						"import lucks.Expr;\n" +
						"\n" +
						"/**\n" +
						" * @author Johannes Herr\n" +
						" */\n" +
						"public interface Visitor<T> {\n" +
						(clazzes.stream()
						.map(c -> String.format("\tT visit%1$s(Expr.%1$s expr);\n", c.name))
						.collect(Collectors.joining(""))) +
						"}\n";

		Files.write(Paths.get("src/main/java/lucks/visitors/Visitor.java"), codeVisitor.getBytes(StandardCharsets.UTF_8));
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
							String.format("\tpublic static class %1$s extends Expr {\n" +
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
											              "\t\tpublic <T> T accept(Visitor<T> visitor) {\n" +
											              "\t\t\treturn visitor.visit%1$s(this);\n" +
											              "\t\t}\n", clazz.name) +

											(String.format("\n" +
															               "\t\t@Override\n" +
															               "\t\tpublic String toString() {\n" +
															               "\t\t\treturn \"%s{\" + %s + \"}\";}",
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
