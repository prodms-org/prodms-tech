package com.hydroyura.prodms.tech.server.service.processstep;

import com.hydroyura.prodms.tech.client.req.ProcessStepCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessStepListReq;
import com.hydroyura.prodms.tech.client.res.ProcessStepCreateRes;
import com.hydroyura.prodms.tech.client.res.ProcessStepListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessStepRes;
import com.hydroyura.prodms.tech.server.db.repository.ProcessRepository;
import com.hydroyura.prodms.tech.server.db.repository.ProcessStepRepository;
import com.hydroyura.prodms.tech.server.props.DefaultParams;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessStepServiceImpl implements ProcessStepService {

    private final ProcessStepRepository repository;
    private final DefaultParams defaultParams;

    @Override
    public Optional<SingleProcessStepRes> get(String number) {
        return Optional.empty();
    }

    @Override
    public ProcessStepListRes list(ProcessStepListReq filter) {
        return null;
    }

    @Override
    public ProcessStepCreateRes create(ProcessStepCreateReq entity) {
        Integer id = repository.create(entity);
        return new ProcessStepCreateRes(id);
    }
}
