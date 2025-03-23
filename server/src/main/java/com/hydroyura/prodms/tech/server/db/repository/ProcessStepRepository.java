package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.client.req.ProcessStepCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessStepListReq;
import com.hydroyura.prodms.tech.client.res.ProcessStepListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessStepRes;
import java.util.Optional;

public interface ProcessStepRepository {

    ProcessStepListRes list(ProcessStepListReq filter);
    Integer create(ProcessStepCreateReq entity);
    Optional<SingleProcessStepRes> get(String number);

}
