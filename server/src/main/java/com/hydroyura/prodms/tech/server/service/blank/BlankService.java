package com.hydroyura.prodms.tech.server.service.blank;

import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.BlankListReq;
import com.hydroyura.prodms.tech.client.res.BlankCreateRes;
import com.hydroyura.prodms.tech.client.res.BlankListRes;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import java.util.Optional;

public interface BlankService {

    Optional<SingleBlankRes> get(String number);
    BlankListRes list(BlankListReq filter);
    BlankCreateRes create(BlankCreateReq equipment);

}
