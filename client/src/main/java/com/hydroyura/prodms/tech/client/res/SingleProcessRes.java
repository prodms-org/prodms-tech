package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import lombok.Data;

@Data
public class SingleProcessRes {

    private Integer id;
    private String number;
    private String unit;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Integer priority;
    private Blank blank;

    @Data
    public static class Blank {
        private Integer id;
        private String number;
        private String material;
    }
}
