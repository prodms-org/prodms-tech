package com.hydroyura.prodms.tech.server.controller.api;

import com.hydroyura.prodms.tech.server.service.equipmentset.EquipmentSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/equipment-sets", produces = MediaType.APPLICATION_JSON_VALUE)
public class EquipmentSetController extends AbstractRestController {

    private final EquipmentSetService equipmentSetService;

}

/*
+ GET /api/v1/equipment-sets - equipment-sets list
+ POST /api/v1/equipment-sets - create new equipment-set
+ GET /api/v1/equipment-sets/{number} - get single equipment-set
+ PATCH /api/v1/equipment-sets/{number}/equipments - add equipments to set
 */
