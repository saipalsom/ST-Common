package org.saipal.common.exception;

public class STNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 6558245097546292566L;

	public STNotFoundException(String message) {
		super(message);
	}

	public STNotFoundException(String message, Exception e) {
		super(message, e);
	}
}
