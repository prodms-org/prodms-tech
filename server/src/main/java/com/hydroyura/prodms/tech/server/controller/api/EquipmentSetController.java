package com.hydroyura.prodms.tech.server.controller.api;

import static com.hydroyura.prodms.common.utils.RestControllerUtils.buildEmptyApiResponse;
import static com.hydroyura.prodms.tech.server.SharedConstants.RESPONSE_ERROR_MSG_EQUIPMENT_SET_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.hydroyura.prodms.common.model.api.ApiRes;
import com.hydroyura.prodms.tech.client.req.EquipmentSetAddEquipmentsReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import com.hydroyura.prodms.tech.server.service.equipmentset.EquipmentSetService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/equipment-sets", produces = MediaType.APPLICATION_JSON_VALUE)
public class EquipmentSetController extends AbstractRestController {

    private final EquipmentSetService equipmentSetService;

    @RequestMapping(method = GET, value = "/{number}")
    public ResponseEntity<ApiRes<?>> get(@PathVariable String number, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentSetService.get(number);

        return result
            .map(apiRes::setData)
            .map(arg -> new ResponseEntity<ApiRes<?>>(arg, HttpStatus.OK))
            .orElseGet(() -> {
                apiRes.getErrors().add(RESPONSE_ERROR_MSG_EQUIPMENT_SET_NOT_FOUND.formatted(number));
                return new ResponseEntity<>(apiRes, HttpStatus.NOT_FOUND);
            });
    }

    @RequestMapping(method = GET, value = "")
    public ResponseEntity<ApiRes<?>> list(EquipmentSetListReq filter, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentSetService.list(filter);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @RequestMapping(method = POST, value = "")
    public ResponseEntity<ApiRes<?>> create(@RequestBody EquipmentSetCreateReq equipmentSet, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentSetService.create(equipmentSet);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @RequestMapping(method = PATCH, value = "/{number}/equipments")
    public ResponseEntity<ApiRes<?>> addEquipments(@PathVariable String number,
                                                   @RequestBody EquipmentSetAddEquipmentsReq equipments,
                                                   HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentSetService.addEquipments(number, equipments);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

}