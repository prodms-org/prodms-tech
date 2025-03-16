package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import lombok.Data;

@Data
public class SingleEquipmentSetRes {
    private Integer id;
    private String number;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<Equipment> equipments = Collections.emptySet();

    @Data
    public static class Equipment {
        private Integer id;
        private String number;
        private String name;
        private Integer type;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }
}
