package com.hydroyura.prodms.tech.client.res;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import lombok.Data;

@Data
public class BlankListRes {

    private Collection<BlankListRes.Blank> blanks = Collections.emptyList();
    private Integer totalCount;

    @Data
    public static class Blank {
        private Integer id;
        private String number;
        private String material;
        private String params;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
    }

}
