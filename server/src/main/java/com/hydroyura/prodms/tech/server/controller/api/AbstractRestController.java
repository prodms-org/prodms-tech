package com.hydroyura.prodms.tech.server.controller.api;

import static com.hydroyura.prodms.common.Constants.REQUEST_ATTR_TIMESTAMP_KEY;
import static com.hydroyura.prodms.common.Constants.REQUEST_ATTR_UUID_KEY;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.web.bind.annotation.ModelAttribute;

public abstract class AbstractRestController {

    @ModelAttribute
    private void requestPreProcess(HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        request.setAttribute(REQUEST_ATTR_UUID_KEY, uuid);
        Timestamp timestamp = Timestamp.from(Instant.now());
        request.setAttribute(REQUEST_ATTR_TIMESTAMP_KEY, timestamp);
    }

}
