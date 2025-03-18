package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.res.BlankListReq;
import com.hydroyura.prodms.tech.client.res.BlankListRes;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import java.util.Optional;

public interface BlankRepository {

    BlankListRes list(BlankListReq filter);
    Integer create(BlankCreateReq blank);
    Optional<SingleBlankRes> get(String number);

}
