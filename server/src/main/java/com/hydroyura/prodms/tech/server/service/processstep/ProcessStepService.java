package com.hydroyura.prodms.tech.server.service.processstep;

import com.hydroyura.prodms.tech.client.req.ProcessStepCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessStepListReq;
import com.hydroyura.prodms.tech.client.res.ProcessStepCreateRes;
import com.hydroyura.prodms.tech.client.res.ProcessStepListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessStepRes;
import java.util.Optional;

public interface ProcessStepService {

    Optional<SingleProcessStepRes> get(String number);
    ProcessStepListRes list(ProcessStepListReq filter);
    ProcessStepCreateRes create(ProcessStepCreateReq entity);

}
