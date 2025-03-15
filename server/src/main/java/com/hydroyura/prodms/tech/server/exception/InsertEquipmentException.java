package com.hydroyura.prodms.tech.server.exception;

public class InsertEquipmentException extends RuntimeException {

    public InsertEquipmentException(String msg) {
        super(msg);
    }

    public InsertEquipmentException(String msg, Throwable e) {
        super(msg, e);
    }

}
