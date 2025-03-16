package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.client.req.EquipmentSetAddEquipmentsReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentSetAddEquipmentsRes;
import com.hydroyura.prodms.tech.client.res.EquipmentSetListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentSetRes;
import java.util.Optional;

public interface EquipmentSetRepository {

    Integer create(EquipmentSetCreateReq equipment);
    EquipmentSetListRes list(EquipmentSetListReq filter);
    Optional<SingleEquipmentSetRes> get(Integer id);
    Optional<SingleEquipmentSetRes> get(String number);
    EquipmentSetAddEquipmentsRes addEquipments(String number, EquipmentSetAddEquipmentsReq equipments);
}