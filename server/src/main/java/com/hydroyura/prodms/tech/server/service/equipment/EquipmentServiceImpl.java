package com.hydroyura.prodms.tech.server.service.equipment;

import com.hydroyura.prodms.tech.client.enums.EquipmentSortCode;
import com.hydroyura.prodms.tech.client.req.EquipmentCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentCreateRes;
import com.hydroyura.prodms.tech.client.res.EquipmentListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentRes;
import com.hydroyura.prodms.tech.server.db.repository.EquipmentRepository;
import com.hydroyura.prodms.tech.server.props.DefaultParams;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final DefaultParams defaultParams;

    @Override
    public Optional<SingleEquipmentRes> get(String number) {
        return equipmentRepository.get(number);
    }

    @Override
    public EquipmentListRes list(EquipmentListReq filter) {
        populateWithDefaultsEquipmentListFilter(filter);
        return equipmentRepository.list(filter);
    }

    @Override
    public EquipmentCreateRes create(EquipmentCreateReq equipment) {
        Integer id = equipmentRepository.create(equipment);
        return new EquipmentCreateRes(id);
    }

    private void populateWithDefaultsEquipmentListFilter(final EquipmentListReq filter) {
        Optional
            .ofNullable(filter.getItemsPerPage())
            .ifPresentOrElse(v -> {}, () -> filter.setItemsPerPage(defaultParams.getEquipmentList().getItemsPerPage()));
        Optional
            .ofNullable(filter.getPage())
            .ifPresentOrElse(v -> {}, () -> filter.setPage(defaultParams.getEquipmentList().getPage()));
        Optional
            .ofNullable(filter.getSortCode())
            .ifPresentOrElse(
                v -> {},
                () -> filter.setSortCode(EquipmentSortCode.getByCode(defaultParams.getEquipmentList().getSortCode())));
    }

}
