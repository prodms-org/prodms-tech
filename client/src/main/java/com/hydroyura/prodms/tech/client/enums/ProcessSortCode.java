package com.hydroyura.prodms.tech.client.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum ProcessSortCode {

    NUMBER_ASC(0, "number", "ASC"),
    NUMBER_DESC(1, "number", "DESC"),
    UNIT_ASC(2, "unit", "ASC"),
    UNIT_DESC(3, "unit", "DESC");

    ProcessSortCode(Integer code, String field, String direction) {
        this.code = code;
        this.field = field;
        this.direction = direction;
    }

    @JsonValue
    private final Integer code;
    private final String field;
    private final String direction;

    // TODO: create custom exception
    // TODO: rebuild it for generic type and replace into utility class
    public static ProcessSortCode getByCode(Integer code) {
        return Arrays
            .stream(ProcessSortCode.values())
            .filter(v -> v.getCode().equals(code))
            .findFirst()
            .orElseThrow();
    }

}
