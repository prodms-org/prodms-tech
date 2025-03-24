package com.hydroyura.prodms.tech.client.req;

import lombok.Data;

@Data
public class ProcessStepCreateReq {

    private String number;
    private String equipmentSetId;
    private String processId;
    private Integer orderNum;
    private String times;

}