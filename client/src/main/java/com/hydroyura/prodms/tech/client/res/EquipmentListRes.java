package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import lombok.Data;

@Data
public class EquipmentListRes {

    private Collection<Equipment> equipments = Collections.emptyList();
    private Integer totalCount;

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
