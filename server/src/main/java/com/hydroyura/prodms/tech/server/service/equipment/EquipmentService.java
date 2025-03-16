package com.hydroyura.prodms.tech.server.service.equipment;

import com.hydroyura.prodms.tech.client.req.EquipmentCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentCreateRes;
import com.hydroyura.prodms.tech.client.res.EquipmentListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentRes;
import java.util.Optional;

public interface EquipmentService {

    Optional<SingleEquipmentRes> get(String number);
    EquipmentListRes list(EquipmentListReq filter);
    EquipmentCreateRes create(EquipmentCreateReq equipment);

}
