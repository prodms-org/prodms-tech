package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.populateField;

import com.hydroyura.prodms.tech.client.req.ProcessStepCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessStepListReq;
import com.hydroyura.prodms.tech.client.res.ProcessStepListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessStepRes;
import com.hydroyura.prodms.tech.server.exception.InsertBlankException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

@RequiredArgsConstructor
@Slf4j
public class ProcessStepRepositoryJdbcTemplateImpl implements ProcessStepRepository {


    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SingleProcessStepRes>
        rowMapperSingle = new ProcessStepRepositoryJdbcTemplateImpl.SingleRowMapper();


    private static final String LOG_MSG_CANNOT_INSERT_PROCESS_STEP = """
            Can not insert new process step with values: number = [%s],
            for process with id = [%s] and equipment set with id = [%s]
        """;


    @Override
    public ProcessStepListRes list(ProcessStepListReq filter) {
        return null;
    }

    @Override
    public Integer create(ProcessStepCreateReq entity) {
        String nativeQuery = """
            INSERT INTO processes_steps (number, equipment_set_id, process_id, order_num, times, created_at, updated_at)
                VALUES (:number, :equipment_set_id, :process_id, :order_num, :times, now(), now())
                RETURNING id
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("number", entity.getNumber())
            .addValue("equipment_set_id", entity.getEquipmentSetId())
            .addValue("process_id", entity.getProcessId())
            .addValue("order_num", entity.getOrderNum())
            .addValue("times", entity.getTimes());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(nativeQuery, params, keyHolder);
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage(), e);
            throw new InsertBlankException(
                LOG_MSG_CANNOT_INSERT_PROCESS_STEP.formatted(
                    entity.getNumber(),
                    entity.getProcessId(),
                    entity.getEquipmentSetId()),
                e);
        }
    }

    @Override
    public Optional<SingleProcessStepRes> get(String number) {
        String nativeQuery = """
            SELECT prs.id AS prs_id, prs.number AS prs_number, prs.order_num AS prs_order_num,
                   prs.created_at AS prs_created_at, prs.updated_at AS prs_updated_at,
                   prs.times AS prs_times,
                   eqs.id AS eqs_id, eqs.number AS eqs_number, eqs.name AS eqs_name,
                   eqs.description AS eqs_description
                FROM processes_steps prs
                    LEFT JOIN equipments_sets as eqs
                        ON prs.equipment_set_id = eqs.id
                WHERE prs.number = :number
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("number", number);
        List<SingleProcessStepRes> resultList = namedParameterJdbcTemplate.query(nativeQuery, params, rowMapperSingle);
        return resultList.stream().findFirst();
    }


    private static class SingleRowMapper implements RowMapper<SingleProcessStepRes> {

        @Override
        public SingleProcessStepRes mapRow(ResultSet rs, int rowNum) throws SQLException {
            SingleProcessStepRes res = new SingleProcessStepRes();
            populateField(rs, res::setId, "prs_id", Integer.class);
            populateField(rs, res::setNumber, "prs_number", String.class);
            populateField(rs, res::setOrder, "prs_order_num", Short.class);
            populateField(rs, res::setCreatedAt, "prs_created_at", OffsetDateTime.class);
            populateField(rs, res::setUpdatedAt, "prs_updated_at", OffsetDateTime.class);
            populateField(rs, res::setTimes, "prs_times", String.class);

            SingleProcessStepRes.EquipmentSet equipmentSet = new SingleProcessStepRes.EquipmentSet();
            populateField(rs, equipmentSet::setId, "eqs_id", Integer.class);
            populateField(rs, equipmentSet::setNumber, "eqs_number", String.class);
            populateField(rs, equipmentSet::setName, "eqs_name", String.class);
            populateField(rs, equipmentSet::setDescription, "eqs_description", String.class);
            res.setEquipmentSet(equipmentSet);

            return res;
        }
    }
}
