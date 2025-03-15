package com.hydroyura.prodms.tech.client.req;

import com.hydroyura.prodms.tech.client.enums.EquipmentSortCode;
import lombok.Data;

@Data
public class EquipmentListReq {

    private Integer itemsPerPage;
    private Integer page;
    private EquipmentSortCode sortCode;
    private String nameLike;
    private String numberLike;

}
