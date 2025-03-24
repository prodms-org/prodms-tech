package com.hydroyura.prodms.tech.server;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SharedConstants {

    public static final String RESPONSE_ERROR_MSG_EQUIPMENT_NOT_FOUND = "Equipment with number = [%s] not found";
    public static final String RESPONSE_ERROR_MSG_EQUIPMENT_SET_NOT_FOUND = "EquipmentSet with number = [%s] not found";
    public static final String RESPONSE_ERROR_MSG_BLANK_NOT_FOUND = "Blank with number = [%s] not found";
    public static final String RESPONSE_ERROR_MSG_PROCESS_NOT_FOUND = "Process with number = [%s] not found";
    public static final String RESPONSE_ERROR_MSG_PROCESS_STEP_NOT_FOUND = """
        Process step with number = [%s] for process with number = [%s] not found
    """;

}
