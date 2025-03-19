package com.hydroyura.prodms.tech.server.controller.api;

import static com.hydroyura.prodms.common.utils.RestControllerUtils.buildEmptyApiResponse;
import static com.hydroyura.prodms.tech.server.SharedConstants.RESPONSE_ERROR_MSG_BLANK_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.hydroyura.prodms.common.model.api.ApiRes;
import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.BlankListReq;
import com.hydroyura.prodms.tech.server.service.blank.BlankService;
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
@RequestMapping(value = "/api/v1/blanks", produces = MediaType.APPLICATION_JSON_VALUE)
public class BlankController extends AbstractRestController {

    private final BlankService blankService;

    @RequestMapping(method = GET, value = "/{number}")
    public ResponseEntity<ApiRes<?>> get(@PathVariable String number, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = blankService.get(number);

        return result
            .map(apiRes::setData)
            .map(arg -> new ResponseEntity<ApiRes<?>>(arg, HttpStatus.OK))
            .orElseGet(() -> {
                apiRes.getErrors().add(RESPONSE_ERROR_MSG_BLANK_NOT_FOUND.formatted(number));
                return new ResponseEntity<>(apiRes, HttpStatus.NOT_FOUND);
            });
    }

    @RequestMapping(method = GET, value = "")
    public ResponseEntity<ApiRes<?>> list(BlankListReq filter, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = blankService.list(filter);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @RequestMapping(method = POST, value = "")
    public ResponseEntity<ApiRes<?>> create(@RequestBody BlankCreateReq equipment, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = blankService.create(equipment);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

}


/*
#### blanks
+ GET /api/v1/blanks - blanks list
+ POST /api/v1/blanks - create new blanks
+ GET /api/v1/blanks/{number} - get single blanks
 */