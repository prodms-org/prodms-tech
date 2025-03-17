package com.hydroyura.prodms.tech.client.res;

import java.util.Collection;
import java.util.Collections;
import lombok.Data;

@Data
public class EquipmentSetAddEquipmentsRes {

    private Integer totalInsertedCount;
    private Collection<Integer> existedEquipmentIds = Collections.emptyList();

}
