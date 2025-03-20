package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Set;
import lombok.Data;

@Data
public class SingleBlankRes {

    private Integer id;
    private String number;
    private String material;
    private String params;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Set<SingleBlankRes.Process> processes = Collections.emptySet();

    @Data
    public static class Process {
        private Integer id;
        private String number;
        private String unit;
    }
}
