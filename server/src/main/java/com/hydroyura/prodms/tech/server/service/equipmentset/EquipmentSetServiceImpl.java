package com.hydroyura.prodms.tech.server.service.equipmentset;

import com.hydroyura.prodms.tech.client.enums.EquipmentSetSortCode;
import com.hydroyura.prodms.tech.client.req.EquipmentSetAddEquipmentsReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentSetAddEquipmentsRes;
import com.hydroyura.prodms.tech.client.res.EquipmentSetCreateRes;
import com.hydroyura.prodms.tech.client.res.EquipmentSetListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentSetRes;
import com.hydroyura.prodms.tech.server.db.repository.EquipmentSetRepository;
import com.hydroyura.prodms.tech.server.props.DefaultParams;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentSetServiceImpl implements EquipmentSetService {

    private final EquipmentSetRepository equipmentSetRepository;
    private final DefaultParams defaultParams;

    @Override
    public Optional<SingleEquipmentSetRes> get(String number) {
        return equipmentSetRepository.get(number);
    }

    @Override
    public EquipmentSetListRes list(EquipmentSetListReq filter) {
        populateWithDefaultsEquipmentListFilter(filter);
        return equipmentSetRepository.list(filter);
    }

    @Override
    public EquipmentSetCreateRes create(EquipmentSetCreateReq equipment) {
        Integer id = equipmentSetRepository.create(equipment);
        return new EquipmentSetCreateRes(id);
    }

    @Override
    public EquipmentSetAddEquipmentsRes addEquipments(String number, EquipmentSetAddEquipmentsReq equipments) {
        return equipmentSetRepository.addEquipments(number, equipments);
    }


    private void populateWithDefaultsEquipmentListFilter(final EquipmentSetListReq filter) {
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
                () -> filter.setSortCode(EquipmentSetSortCode.getByCode(defaultParams.getEquipmentSetList().getSortCode())));
    }

}
