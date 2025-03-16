package com.hydroyura.prodms.tech.server.service.equipmentset;

import com.hydroyura.prodms.tech.server.db.repository.EquipmentSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipmentSetServiceImpl implements EquipmentSetService {

    private final EquipmentSetRepository equipmentSetRepository;

}
