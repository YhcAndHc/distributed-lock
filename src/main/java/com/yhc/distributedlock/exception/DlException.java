package com.yhc.distributedlock.exception;

public class DlException extends RuntimeException {

	private static final long serialVersionUID = 8515495008247474916L;

	private String msg;

	public DlException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

}
