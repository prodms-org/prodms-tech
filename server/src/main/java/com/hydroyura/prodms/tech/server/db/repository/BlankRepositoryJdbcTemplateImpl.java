package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.res.BlankListReq;
import com.hydroyura.prodms.tech.client.res.BlankListRes;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Slf4j
@RequiredArgsConstructor
public class BlankRepositoryJdbcTemplateImpl implements BlankRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    // private final RowMapper<SingleEquipmentRes> rowMapperSingleEquipment = new EquipmentRepositoryJdbcTemplateImpl.SingleEquipmentRowMapper();
    // private final RowMapper<EquipmentListRes.Equipment> rowMapperEquipmentList = new EquipmentRepositoryJdbcTemplateImpl.EquipmentListRowMapper();

    @Override
    public BlankListRes list(BlankListReq filter) {
        return null;
    }

    @Override
    public Integer create(BlankCreateReq blank) {
        return 0;
    }

    @Override
    public Optional<SingleBlankRes> get(String number) {
        return Optional.empty();
    }
}