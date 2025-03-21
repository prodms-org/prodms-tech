package com.hydroyura.prodms.tech.client.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum BlankSortCode {

    NUMBER_ASC(0, "number", "ASC"),
    NUMBER_DESC(1, "number", "DESC"),
    MATERIAL_ASC(2, "material", "ASC"),
    MATERIAL_DESC(3, "material", "DESC");

    BlankSortCode(Integer code, String field, String direction) {
        this.code = code;
        this.field = field;
        this.direction = direction;
    }

    @JsonValue
    private final Integer code;
    private final String field;
    private final String direction;

    // TODO: create custom exception
    public static BlankSortCode getByCode(Integer code) {
        return Arrays
            .stream(BlankSortCode.values())
            .filter(v -> v.getCode().equals(code))
            .findFirst()
            .orElseThrow();
    }

}
