package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.populateField;

import com.hydroyura.prodms.tech.client.req.ProcessCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessListReq;
import com.hydroyura.prodms.tech.client.res.ProcessListRes;
import com.hydroyura.prodms.tech.client.res.SingleProcessRes;
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
public class ProcessRepositoryJdbcTemplateImpl implements ProcessRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SingleProcessRes>
        rowMapperSingle = new ProcessRepositoryJdbcTemplateImpl.SingleRowMapper();
/*
    private final RowMapper<EquipmentListRes.Equipment> rowMapperEquipmentList = new EquipmentRepositoryJdbcTemplateImpl.EquipmentListRowMapper();
*/

    private static final String LOG_MSG_CANNOT_INSERT_PROCESS = """
            Can not insert new process with values: number = [%s], for unit with number = [%s]
        """;


    @Override
    public ProcessListRes list(ProcessListReq filter) {
        return null;
    }

    @Override
    public Integer create(ProcessCreateReq entity) {
        String nativeQuery = """
            INSERT INTO processes (number, unit, created_at, updated_at, priority, blank_id)
                VALUES (:number, :unit, now(), now(), :priority, :blank_id)
                RETURNING id
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("number", entity.getNumber())
            .addValue("unit", entity.getUnit())
            .addValue("priority", entity.getPriority())
            .addValue("blank_id", entity.getBlankId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(nativeQuery, params, keyHolder);
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage(), e);
            throw new InsertBlankException(
                LOG_MSG_CANNOT_INSERT_PROCESS.formatted(
                    entity.getNumber(),
                    entity.getUnit()),
                e);
        }
    }

    @Override
    public Optional<SingleProcessRes> get(String number) {
        String nativeQuery = """
            SELECT pr.id AS pr_id, pr.number AS pr_number, pr.unit AS pr_unit,
                   pr.created_at AS pr_created_at, pr.updated_at AS pr_updated_at,
                   pr.priority AS pr_priority,
                   b.id AS b_id, b.number AS b_number, b.material AS b_material
                FROM processes pr
                    LEFT JOIN blanks as b
                        ON pr.blank_id = b.id
                WHERE pr.number = :number
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("number", number);
        List<SingleProcessRes> resultList = namedParameterJdbcTemplate.query(nativeQuery, params, rowMapperSingle);
        return resultList.stream().findFirst();
    }


    private static class SingleRowMapper implements RowMapper<SingleProcessRes> {

        @Override
        public SingleProcessRes mapRow(ResultSet rs, int rowNum) throws SQLException {
            SingleProcessRes res = new SingleProcessRes();
            populateField(rs, res::setId, "pr_id", Integer.class);
            populateField(rs, res::setNumber, "pr_number", String.class);
            populateField(rs, res::setUnit, "pr_unit", String.class);
            populateField(rs, res::setCreatedAt, "pr_created_at", OffsetDateTime.class);
            populateField(rs, res::setUpdatedAt, "pr_updated_at", OffsetDateTime.class);
            populateField(rs, res::setPriority, "pr_priority", Integer.class);

            SingleProcessRes.Blank blank = new SingleProcessRes.Blank();
            populateField(rs, blank::setId, "b_id", Integer.class);
            populateField(rs, blank::setNumber, "b_number", String.class);
            populateField(rs, blank::setMaterial, "b_material", String.class);
            res.setBlank(blank);

            return res;
        }
    }
}
