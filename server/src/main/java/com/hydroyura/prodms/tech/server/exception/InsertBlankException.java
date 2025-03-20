package com.hydroyura.prodms.tech.server.exception;

public class InsertBlankException extends RuntimeException {

    public InsertBlankException(String msg) {
        super(msg);
    }

    public InsertBlankException(String msg, Throwable e) {
        super(msg, e);
    }

}
