package lucks;

public class Return extends RuntimeException {

	private final Object val;

	public Return(Object val) {
		super(null, null, false, false);
		this.val = val;
	}

	public Object getVal() {
		return val;
	}
}
