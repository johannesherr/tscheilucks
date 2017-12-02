package lucks;

import java.util.List;

import lucks.visitors.Interpreter;

public interface LoxCallable {

	int arity();

	Object call(Interpreter interpreter, List<Object> params);

}
