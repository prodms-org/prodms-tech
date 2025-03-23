package com.hydroyura.prodms.tech.server.controller.api;

import static com.hydroyura.prodms.common.utils.RestControllerUtils.buildEmptyApiResponse;
import static com.hydroyura.prodms.tech.server.SharedConstants.RESPONSE_ERROR_MSG_PROCESS_NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.hydroyura.prodms.common.model.api.ApiRes;
import com.hydroyura.prodms.tech.client.req.ProcessAddProcessStepsReq;
import com.hydroyura.prodms.tech.client.req.ProcessCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessListReq;
import com.hydroyura.prodms.tech.server.service.process.ProcessService;
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
@RequestMapping(value = "/api/v1/processes", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProcessController extends AbstractRestController {

    private final ProcessService service;

    @RequestMapping(method = GET, value = "/{number}")
    public ResponseEntity<ApiRes<?>> get(@PathVariable String number, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = service.get(number);

        return result
            .map(apiRes::setData)
            .map(arg -> new ResponseEntity<ApiRes<?>>(arg, HttpStatus.OK))
            .orElseGet(() -> {
                apiRes.getErrors().add(RESPONSE_ERROR_MSG_PROCESS_NOT_FOUND.formatted(number));
                return new ResponseEntity<>(apiRes, HttpStatus.NOT_FOUND);
            });
    }

    @RequestMapping(method = GET, value = "")
    public ResponseEntity<ApiRes<?>> list(ProcessListReq filter, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = service.list(filter);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @RequestMapping(method = POST, value = "")
    public ResponseEntity<ApiRes<?>> create(@RequestBody ProcessCreateReq body, HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);
        var result = service.create(body);

        apiRes.setData(result);
        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

    @RequestMapping(method = PATCH, value = "/{number}/process-steps")
    private ResponseEntity<ApiRes<?>> addProcessSteps(@PathVariable String number,
                                                      @RequestBody ProcessAddProcessStepsReq body,
                                                      HttpServletRequest request) {
        var apiRes = buildEmptyApiResponse(request);

        return new ResponseEntity<ApiRes<?>>(apiRes, HttpStatus.OK);
    }

}


/*
+ GET /api/v1/processes - processes list
+ POST /api/v1/processes - create new process
+ GET /api/v1/processes/{number} - get single process
+ PATCH /api/v1/processes/{number}/process-steps - add step to process
 */