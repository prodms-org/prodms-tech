package com.hydroyura.prodms.tech.server.controller.api;

import static com.hydroyura.prodms.common.Constants.REQUEST_ATTR_TIMESTAMP_KEY;
import static com.hydroyura.prodms.common.Constants.REQUEST_ATTR_UUID_KEY;
import static com.hydroyura.prodms.common.utils.RestControllerUtils.buildEmptyApiResponse;
import static com.hydroyura.prodms.tech.server.SharedConstants.RESPONSE_ERROR_MSG_EQUIPMENT_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.hydroyura.prodms.common.model.api.ApiRes;
import com.hydroyura.prodms.tech.client.req.EquipmentCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentListReq;
import com.hydroyura.prodms.tech.server.service.equipment.EquipmentService;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/equipments", produces = MediaType.APPLICATION_JSON_VALUE)
public class EquipmentController {

    private final EquipmentService equipmentService;

    @RequestMapping(method = GET, value = "/{number}")
    public ResponseEntity<ApiRes<?>> get(@PathVariable String number, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentService.get(number);

        return result
            .map(apiRes::setData)
            .map(arg -> new ResponseEntity<ApiRes<?>>(arg, HttpStatus.OK))
            .orElseGet(() -> {
                apiRes.getErrors().add(RESPONSE_ERROR_MSG_EQUIPMENT_NOT_FOUND.formatted(number));
                return new ResponseEntity<>(apiRes, HttpStatus.NOT_FOUND);
            });
    }

    @RequestMapping(method = GET, value = "")
    public ResponseEntity<ApiRes<?>> list(EquipmentListReq filter, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentService.list(filter);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @RequestMapping(method = POST, value = "")
    public ResponseEntity<ApiRes<?>> create(@RequestBody EquipmentCreateReq equipment, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = equipmentService.create(equipment);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @ModelAttribute
    private void requestPreProcess(HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        request.setAttribute(REQUEST_ATTR_UUID_KEY, uuid);
        Timestamp timestamp = Timestamp.from(Instant.now());
        request.setAttribute(REQUEST_ATTR_TIMESTAMP_KEY, timestamp);
    }

}
