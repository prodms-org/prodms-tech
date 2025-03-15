package com.hydroyura.prodms.tech.server.db.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;

@Data
public class Equipment {

    private Integer id;
    private String number;
    private String name;
    private Short type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<EquipmentSet> equipmentSets = new HashSet<>();

}