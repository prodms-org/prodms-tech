package com.hydroyura.prodms.tech.client.enums;

import lombok.Getter;

@Getter
public enum EquipmentType {

    MANUAL(1), MACHINE(2), SMART_MACHINE(3);

    EquipmentType(Integer code) {
        this.code = code;
    }

    private final Integer code;

}
