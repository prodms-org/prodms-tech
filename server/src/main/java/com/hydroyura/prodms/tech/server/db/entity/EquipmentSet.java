package com.hydroyura.prodms.tech.server.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
@Entity
@Table(name = "equipments_sets")
public class EquipmentSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String descriptions;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "equipments_sets_composition",
        joinColumns = @JoinColumn(name = "equipment_id"),
        inverseJoinColumns = @JoinColumn(name = "set_id")
    )
    private Set<Equipment> equipments;

    @OneToMany(mappedBy = "equipmentSet")
    private List<ProcessStep> processSteps;

}
