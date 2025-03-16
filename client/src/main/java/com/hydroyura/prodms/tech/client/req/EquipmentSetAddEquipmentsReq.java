package com.hydroyura.prodms.tech.client.req;

import java.util.Collection;
import java.util.Collections;
import lombok.Data;

@Data
public class EquipmentSetAddEquipmentsReq {
    private Collection<String> numbers = Collections.emptyList();
}
