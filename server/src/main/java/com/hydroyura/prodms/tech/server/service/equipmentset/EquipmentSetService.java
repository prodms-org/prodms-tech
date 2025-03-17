package com.hydroyura.prodms.tech.server.service.equipmentset;

import com.hydroyura.prodms.tech.client.req.EquipmentSetCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentSetCreateRes;
import com.hydroyura.prodms.tech.client.res.EquipmentSetListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentSetRes;
import java.util.Optional;

public interface EquipmentSetService {

    Optional<SingleEquipmentSetRes> get(String number);
    EquipmentSetListRes list(EquipmentSetListReq filter);
    EquipmentSetCreateRes create(EquipmentSetCreateReq equipment);
}
