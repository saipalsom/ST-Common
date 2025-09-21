package org.saipal.common.exception;

public class STRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 6558245097546292566L;

	public STRuntimeException(String message) {
		super(message);
	}

	public STRuntimeException(String message, Exception e) {
		super(message, e);
	}
}
