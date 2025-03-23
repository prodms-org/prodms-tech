package com.hydroyura.prodms.tech.server.service.process;

import com.hydroyura.prodms.tech.client.enums.ProcessSortCode;
import com.hydroyura.prodms.tech.client.req.ProcessAddProcessStepsReq;
import com.hydroyura.prodms.tech.client.req.ProcessCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessListReq;
import com.hydroyura.prodms.tech.client.res.ProcessAddProcessStepsRes;
import com.hydroyura.prodms.tech.client.res.ProcessCreateRes;
import com.hydroyura.prodms.tech.client.res.ProcessListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessRes;
import com.hydroyura.prodms.tech.server.db.repository.ProcessRepository;
import com.hydroyura.prodms.tech.server.props.DefaultParams;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final ProcessRepository repository;
    private final DefaultParams defaultParams;

    @Override
    public Optional<SingleProcessRes> get(String number) {
        return repository.get(number);
    }

    @Override
    public ProcessListRes list(ProcessListReq filter) {
        populateWithDefaultsEquipmentListFilter(filter);
        return repository.list(filter);
    }

    @Override
    public ProcessCreateRes create(ProcessCreateReq entity) {
        Integer id = repository.create(entity);
        return new ProcessCreateRes(id);
    }

    @Override
    public ProcessAddProcessStepsRes addProcessSteps(String number, ProcessAddProcessStepsReq processSteps) {
        return null;
    }


    private void populateWithDefaultsEquipmentListFilter(final ProcessListReq filter) {
        Optional
            .ofNullable(filter.getItemsPerPage())
            .ifPresentOrElse(v -> {}, () -> filter.setItemsPerPage(defaultParams.getEquipmentSetList().getItemsPerPage()));
        Optional
            .ofNullable(filter.getPage())
            .ifPresentOrElse(v -> {}, () -> filter.setPage(defaultParams.getEquipmentSetList().getPage()));
        Optional
            .ofNullable(filter.getSortCode())
            .ifPresentOrElse(
                v -> {},
                () -> filter.setSortCode(ProcessSortCode.getByCode(defaultParams.getProcessList().getSortCode())));
    }

}
