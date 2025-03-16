package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import lombok.Data;

@Data
public class EquipmentSetListRes {

    private Collection<EquipmentSetListRes.EquipmentSet> sets = Collections.emptyList();
    private Integer totalCount;

    @Data
    public static class EquipmentSet {
        private Integer id;
        private String number;
        private String name;
        private String description;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

}
