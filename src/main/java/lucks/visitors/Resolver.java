package lucks.visitors;

import lucks.Expr;
import lucks.Lox;
import lucks.Stmt;
import lucks.Token;

import java.util.*;

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
		for (int cnt = 0; iterator.hasNext(); cnt++) {
			if (iterator.next().containsKey(name.getLexeme())) {
				interpreter.resolve(name, cnt);
				return;
			}
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
		if (enclosingClass == ClassType.NONE) {
			Lox.error(expr.keyword, "this is only allowed in methods");
		}
		resolveLocal(expr.keyword);
		return null;
	}

	@Override
	public Void visitSuper(Expr.Super expr) {
		if (enclosingClass == ClassType.NONE) {
			Lox.error(expr.zuper, "Cannot use 'super' outside of a class.");
		}
		if (enclosingClass == ClassType.CLASS) {
			Lox.error(expr.zuper, "Cannot use 'super' in a class with no superclass.");
		}
		resolveLocal(expr.zuper);
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
			if (enclosingFunction == FunctionType.INITIALIZER) {
				Lox.error(stmt.keyword, "Cannot return a value from a constructor.");
			}
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

		Token superClass = stmt.superClass;
		if (superClass != null) {
			resolveLocal(superClass);
			enterScope().put("super", true);
		}

		enterScope().put("this", true);

		ClassType parent = this.enclosingClass;
		this.enclosingClass = superClass != null ? ClassType.SUBCLASS : ClassType.CLASS;
		for (Stmt.FunDecl method : stmt.methods) {
			boolean isConstructor = method.name.getLexeme().equals("init");
			resolveFunction(method, isConstructor ? FunctionType.INITIALIZER : FunctionType.METHOD);
		}
		this.enclosingClass = parent;

		exitScope();

		if (superClass != null) exitScope();

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

	private HashMap<String, Boolean> enterScope() {
		HashMap<String, Boolean> scope = new HashMap<>();
		scopes.addLast(scope);
		return scope;
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
		NONE, FUNCTION, METHOD, INITIALIZER
	}
	private enum ClassType {
		NONE, SUBCLASS, CLASS
	}
}
