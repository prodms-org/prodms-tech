package com.hydroyura.prodms.tech.server.db.repository;

import java.util.Optional;
import com.hydroyura.prodms.tech.client.req.EquipmentCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentRes;

public interface EquipmentRepository {

    EquipmentListRes list(EquipmentListReq filter);
    Integer create(EquipmentCreateReq equipment);
    Optional<SingleEquipmentRes> get(Integer id);
    Optional<SingleEquipmentRes> get(String number);

}
