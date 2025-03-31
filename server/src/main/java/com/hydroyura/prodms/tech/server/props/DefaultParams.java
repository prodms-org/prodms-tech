package com.hydroyura.prodms.tech.server.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "defaults")
public class DefaultParams {

    private EquipmentList equipmentList;
    private EquipmentSetList equipmentSetList;
    private BlankList blankList;
    private ProcessList processList;
    private ProcessStepList processStepList;

    @Data
    public static class EquipmentList {
        private Integer itemsPerPage;
        private Integer page;
        private Integer sortCode;
    }

    @Data
    public static class EquipmentSetList {
        private Integer itemsPerPage;
        private Integer page;
        private Integer sortCode;
    }

    @Data
    public static class BlankList {
        private Integer itemsPerPage;
        private Integer page;
        private Integer sortCode;
    }

    @Data
    public static class ProcessList {
        private Integer itemsPerPage;
        private Integer page;
        private Integer sortCode;
    }

    @Data
    public static class ProcessStepList {
        private Integer itemsPerPage;
        private Integer page;
        private Integer sortCode;
    }

}
