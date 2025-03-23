package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.client.req.ProcessCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessListReq;
import com.hydroyura.prodms.tech.client.res.ProcessListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessRes;
import java.util.Optional;

public interface ProcessRepository {

    ProcessListRes list(ProcessListReq filter);
    Integer create(ProcessCreateReq entity);
    Optional<SingleProcessRes> get(String number);

}
