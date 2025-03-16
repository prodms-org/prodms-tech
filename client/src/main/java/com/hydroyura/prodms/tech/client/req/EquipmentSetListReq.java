package com.hydroyura.prodms.tech.client.req;

import com.hydroyura.prodms.tech.client.enums.EquipmentSetSortCode;
import lombok.Data;

@Data
public class EquipmentSetListReq {

    private Integer itemsPerPage;
    private Integer page;
    private EquipmentSetSortCode sortCode;
    private String nameLike;
    private String numberLike;

}
