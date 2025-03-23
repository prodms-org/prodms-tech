package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class SingleProcessStepRes {

    private Integer id;
    private String number;
    private EquipmentSet equipmentSet;
    private Short order;
    private String times;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Data
    public static class EquipmentSet {
        private Integer id;
        private String number;
        private String name;
        private String description;
    }

}
