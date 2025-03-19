package com.hydroyura.prodms.tech.client.req;

import com.hydroyura.prodms.tech.client.enums.BlankSortCode;
import lombok.Data;

@Data
public class BlankListReq {

    private Integer itemsPerPage;
    private Integer page;
    private BlankSortCode sortCode;
    private String nameLike;
    private String numberLike;

}
