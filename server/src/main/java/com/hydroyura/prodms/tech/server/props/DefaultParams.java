package com.hydroyura.prodms.tech.server.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "defaults")
public class DefaultParams {

    private EquipmentList equipmentList;
    private EquipmentSetList equipmentSetList;
    private BlankList blankList;

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

}
