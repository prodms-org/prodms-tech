package com.hydroyura.prodms.tech.client.req;

import com.hydroyura.prodms.tech.client.enums.ProcessSortCode;
import lombok.Data;

@Data
public class ProcessListReq {

    private Integer itemsPerPage;
    private Integer page;
    private ProcessSortCode sortCode;
    private String nameLike;
    private String numberLike;

}
