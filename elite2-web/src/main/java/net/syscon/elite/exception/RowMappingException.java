package net.syscon.elite.exception;

public class RowMappingException extends  RuntimeException {

	public RowMappingException() {
	}

	public RowMappingException(String s) {
		super(s);
	}

	public RowMappingException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public RowMappingException(Throwable throwable) {
		super(throwable);
	}

	public RowMappingException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
