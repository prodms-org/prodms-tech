package com.hydroyura.prodms.tech.server.service.process;

import com.hydroyura.prodms.tech.client.req.ProcessAddProcessStepsReq;
import com.hydroyura.prodms.tech.client.req.ProcessCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessListReq;
import com.hydroyura.prodms.tech.client.res.ProcessAddProcessStepsRes;
import com.hydroyura.prodms.tech.client.res.ProcessCreateRes;
import com.hydroyura.prodms.tech.client.res.ProcessListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessRes;
import java.util.Optional;

public interface ProcessService {

    Optional<SingleProcessRes> get(String number);
    ProcessListRes list(ProcessListReq filter);
    ProcessCreateRes create(ProcessCreateReq entity);
    ProcessAddProcessStepsRes addProcessSteps(String number, ProcessAddProcessStepsReq processSteps);

}
