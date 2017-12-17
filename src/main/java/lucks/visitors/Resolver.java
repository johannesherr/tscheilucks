package lucks.visitors;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucks.Expr;
import lucks.Lox;
import lucks.Stmt;
import lucks.Token;

public class Resolver implements Stmt.Visitor<Void>, Expr.Visitor<Void> {

	private final ArrayDeque<Map<String, Boolean>> scopes = new ArrayDeque<>();
	private final Interpreter interpreter;
	private FunctionType enclosingFunction = FunctionType.NONE;
	private ClassType enclosingClass = ClassType.NONE;

	public Resolver(Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	@Override
	public Void visitBinary(Expr.Binary expr) {
		resolve(expr.left);
		resolve(expr.right);
		return null;
	}

	@Override
	public Void visitUnary(Expr.Unary expr) {
		resolve(expr.expr);
		return null;
	}

	@Override
	public Void visitLiteral(Expr.Literal expr) {
		return null;
	}

	@Override
	public Void visitVariable(Expr.Variable expr) {
		Token name = expr.name;
		if (!scopes.isEmpty() && scopes.peekLast().get(name.getLexeme()) == Boolean.FALSE) {
			Lox.error(name, "Variable declared, but not defined.");
		}

		resolveLocal(name);
		return null;
	}

	private void resolveLocal(Token name) {
		Iterator<Map<String, Boolean>> iterator = scopes.descendingIterator();
		int cnt = 0;
		while (iterator.hasNext()) {
			if (iterator.next().containsKey(name.getLexeme())) {
				interpreter.resolve(name, cnt);
				return;
			}
			cnt++;
		}
	}

	@Override
	public Void visitGrouping(Expr.Grouping expr) {
		resolve(expr.expr);
		return null;
	}

	@Override
	public Void visitCall(Expr.Call expr) {
		resolve(expr.callee);
		for (Expr argument : expr.arguments) {
			resolve(argument);
		}

		return null;
	}

	@Override
	public Void visitSet(Expr.Set expr) {
		resolve(expr.object);
		resolve(expr.value);
		return null;
	}

	@Override
	public Void visitThis(Expr.This expr) {
		if (enclosingClass != ClassType.CLASS) {
			Lox.error(expr.keyword, "this is only allowed in methods");
		}
		resolveLocal(expr.keyword);
		return null;
	}

	@Override
	public Void visitExpression(Stmt.Expression stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitPrint(Stmt.Print stmt) {
		resolve(stmt.expression);
		return null;
	}

	@Override
	public Void visitBlock(Stmt.Block stmt) {
		enterScope();
		resolveBlock(stmt.stmts);
		exitScope();
		return null;
	}

	@Override
	public Void visitVar(Stmt.Var stmt) {
		declare(stmt.name);
		if (stmt.initializer != null) {
			resolve(stmt.initializer);
		}
		define(stmt.name);
		return null;
	}

	@Override
	public Void visitFunDecl(Stmt.FunDecl stmt) {
		declare(stmt.name);
		define(stmt.name);

		resolveFunction(stmt, FunctionType.FUNCTION);

		return null;
	}

	private void resolveFunction(Stmt.FunDecl stmt, FunctionType functionType) {
		FunctionType parent = this.enclosingFunction;
		try {
			enclosingFunction = functionType;

			enterScope();
			for (Token parameter : stmt.parameters) {
				declare(parameter);
				define(parameter);
			}

			resolveBlock(stmt.body);
			exitScope();

		} finally {
			enclosingFunction = parent;
		}
	}

	@Override
	public Void visitReturn(Stmt.Return stmt) {
		if (enclosingFunction == FunctionType.NONE) {
			Lox.error(stmt.keyword, "return only allowed in functions");
		}
		if (stmt.value != null) {
			resolve(stmt.value);
		}
		return null;
	}

	@Override
	public Void visitIf(Stmt.If stmt) {
		resolve(stmt.cond);
		resolve(stmt.thenBranch);
		if (stmt.elseBranch != null) {
			resolve(stmt.elseBranch);
		}
		return null;
	}

	@Override
	public Void visitWhile(Stmt.While stmt) {
		resolve(stmt.cond);
		resolve(stmt.body);

		return null;
	}

	@Override
	public Void visitClass(Stmt.Class stmt) {
		declare(stmt.name);
		define(stmt.name);

		enterScope();
		scopes.peekLast().put("this", true);

		ClassType parent = this.enclosingClass;
		this.enclosingClass = ClassType.CLASS;
		for (Stmt.FunDecl method : stmt.methods) {
			resolveFunction(method, FunctionType.METHOD);
		}
		this.enclosingClass = parent;

		exitScope();

		return null;
	}

	public void resolveBlock(List<Stmt> stmts) {
		for (Stmt stmt : stmts) {
			resolve(stmt);
		}
	}

	private void resolve(Stmt stmt) {
		stmt.accept(this);
	}

	private void resolve(Expr expr) {
		expr.accept(this);
	}

	private void enterScope() {
		scopes.addLast(new HashMap<>());
	}

	private void exitScope() {
		scopes.removeLast();
	}

	private void declare(Token name) {
		if (!scopes.isEmpty()) {
			Map<String, Boolean> current = scopes.peekLast();
			if (current.containsKey(name.getLexeme())) {
				Lox.error(name, "Variable already declared in scope.");
			}
			current.put(name.getLexeme(), false);
		}
	}

	private void define(Token name) {
		if (!scopes.isEmpty()) {
			scopes.peekLast().put(name.getLexeme(), true);
		}
	}

	private enum FunctionType {
		NONE, FUNCTION, METHOD
	}
	private enum ClassType {
		NONE, CLASS
	}
}
