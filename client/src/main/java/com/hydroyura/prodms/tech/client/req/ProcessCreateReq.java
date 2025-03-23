package com.hydroyura.prodms.tech.client.req;

import lombok.Data;

@Data
public class ProcessCreateReq {

    private String number;
    private String unit;
    private Integer priority;
    private Integer blankId;

}
