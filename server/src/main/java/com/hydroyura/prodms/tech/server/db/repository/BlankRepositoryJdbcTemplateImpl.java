package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.populateField;

import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.BlankListReq;
import com.hydroyura.prodms.tech.client.res.BlankListRes;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

@Slf4j
@RequiredArgsConstructor
public class BlankRepositoryJdbcTemplateImpl implements BlankRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<SingleBlankRes> rowMapperSingleBlank = new BlankRepositoryJdbcTemplateImpl.SingleBlankRowMapper();
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
        String nativeQuery = """
            SELECT b.id AS b_id, b.number AS b_number, b.material AS b_material,
                   b.params AS b_params, b.created_at AS b_created_at, b.updated_at AS b_updated_at,
                   pr.id AS pr_id, pr.number AS pr_number, pr.unit AS pr_unit
                FROM blanks b
                    LEFT JOIN processes as pr
                        ON b.id = pr.blank_id
                WHERE b.number = :number
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("number", number);
        List<SingleBlankRes> resultList = namedParameterJdbcTemplate.query(nativeQuery, params, rowMapperSingleBlank);
        return resultList.stream().findFirst();
    }



    private static class SingleBlankRowMapper implements RowMapper<SingleBlankRes> {

        @Override
        public SingleBlankRes mapRow(ResultSet rs, int rowNum) throws SQLException {
            SingleBlankRes blank = new SingleBlankRes();
            populateField(rs, blank::setId, "b_id", Integer.class);
            populateField(rs, blank::setNumber, "b_number", String.class);
            populateField(rs, blank::setMaterial, "b_material", String.class);
            populateField(rs, blank::setParams, "b_params", String.class);
            populateField(rs, blank::setCreatedAt, "b_created_at", OffsetDateTime.class);
            populateField(rs, blank::setUpdatedAt, "b_updated_at", OffsetDateTime.class);

            Set<SingleBlankRes.Process> processes = new HashSet<>();
            do {
                if (Objects.nonNull(rs.getObject("pr_id", Integer.class))) {
                    SingleBlankRes.Process process = new SingleBlankRes.Process();
                    populateField(rs, process::setId, "pr_id", Integer.class);
                    populateField(rs, process::setNumber, "pr_number", String.class);
                    populateField(rs, process::setUnit, "pr_unit", String.class);
                    processes.add(process);
                }
            } while (rs.next());

            blank.setProcesses(processes);
            return blank;
        }
    }
}