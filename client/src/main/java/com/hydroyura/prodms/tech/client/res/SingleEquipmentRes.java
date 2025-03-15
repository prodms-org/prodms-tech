package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import lombok.Data;

@Data
public class SingleEquipmentRes {

    private Integer id;
    private String number;
    private String name;
    private Integer type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<EquipmentSet> sets = Collections.emptySet();

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
