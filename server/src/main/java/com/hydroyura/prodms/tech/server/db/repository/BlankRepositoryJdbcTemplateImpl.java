package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.buildSortAndPagination;
import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.populateField;

import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.BlankListReq;
import com.hydroyura.prodms.tech.client.res.BlankListRes;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import com.hydroyura.prodms.tech.server.exception.InsertBlankException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
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

@Slf4j
@RequiredArgsConstructor
public class BlankRepositoryJdbcTemplateImpl implements BlankRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<SingleBlankRes> rowMapperSingleBlank = new BlankRepositoryJdbcTemplateImpl.SingleBlankRowMapper();
    private final RowMapper<BlankListRes.Blank> rowMapperEquipmentList = new BlankRepositoryJdbcTemplateImpl.BlankListRowMapper();

    private static final String LOG_MSG_CANNOT_INSERT_BLANK = """
            Can not insert new blank with values: number = [%s], material = [%s]
        """;

    @Override
    public BlankListRes list(BlankListReq filter) {
        String nativeBaseQueryCount = """
            SELECT COUNT(*) FROM blanks b
            """;

        List<BlankRepositoryJdbcTemplateImpl.Predicate> predicates = new ArrayList<>();

        Optional
            .ofNullable(filter.getNumberLike())
            .map(v -> new BlankRepositoryJdbcTemplateImpl.Predicate("b.number", v, BlankRepositoryJdbcTemplateImpl.Operation.LIKE))
            .ifPresent(predicates::add);
        Optional
            .ofNullable(filter.getNameLike())
            .map(v -> new BlankRepositoryJdbcTemplateImpl.Predicate("b.name", v, BlankRepositoryJdbcTemplateImpl.Operation.LIKE))
            .ifPresent(predicates::add);

        StringBuilder sbCount = new StringBuilder(nativeBaseQueryCount);
        if (!predicates.isEmpty()) {
            sbCount.append(" WHERE ");
            String countCondition = predicates
                .stream()
                .map(BlankRepositoryJdbcTemplateImpl.Predicate::buildCondition)
                .collect(Collectors.joining(" AND "));
            sbCount.append(countCondition);
        }
        String nativeQueryCount = sbCount.toString();
        Integer totalCount = jdbcTemplate.queryForObject(nativeQueryCount, Integer.class);

        String nativeBaseQueryValues = """
            SELECT b.id AS b_id, b.number AS b_number, b.material AS b_material, b.params AS b_params,
                   b.created_at AS b_created_at, b.updated_at AS b_updated_at
               FROM blanks b
            """;
        StringBuilder sbValues = new StringBuilder(nativeBaseQueryValues);
        if (!predicates.isEmpty()) {
            sbValues.append(" WHERE ");
            String countCondition = predicates
                .stream()
                .map(BlankRepositoryJdbcTemplateImpl.Predicate::buildCondition)
                .collect(Collectors.joining(" AND "));
            sbValues.append(countCondition);
        }
        String pagination = buildPagination(filter);
        sbValues.append(pagination);
        String nativeQueryValues = sbValues.toString();
        List<BlankListRes.Blank> values = jdbcTemplate.query(nativeQueryValues, rowMapperEquipmentList);

        BlankListRes response = new BlankListRes();
        response.setBlanks(values);
        response.setTotalCount(totalCount);
        return response;
    }

    private String buildPagination(BlankListReq req) {
        return buildSortAndPagination(
            req.getSortCode().getField(),
            req.getSortCode().getDirection(),
            req.getItemsPerPage(),
            req.getPage() * req.getItemsPerPage()
        );
    }

    @AllArgsConstructor
    private static class Predicate {
        private String field;
        private String value;
        private BlankRepositoryJdbcTemplateImpl.Operation operation;

        public String buildCondition() {
            return field + " " + operation.getValue() + " " + value;
        }
    }

    @Getter
    private enum Operation {
        LIKE("LIKE");

        Operation(String value) {
            this.value = value;
        }

        private final String value;
    }

    @Override
    public Integer create(BlankCreateReq blank) {
        String nativeQuery = """
            INSERT INTO blanks (number, material, params, created_at, updated_at)
                VALUES (:number, :material, :params::jsonb, now(), now())
                RETURNING id
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("number", blank.getNumber())
            .addValue("material", blank.getMaterial())
            .addValue("params", blank.getParams());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(nativeQuery, params, keyHolder);
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage(), e);
            throw new InsertBlankException(
                LOG_MSG_CANNOT_INSERT_BLANK.formatted(
                    blank.getNumber(),
                    blank.getMaterial()),
                e);
        }
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

    private static class BlankListRowMapper implements RowMapper<BlankListRes.Blank> {

        @Override
        public BlankListRes.Blank mapRow(ResultSet rs, int rowNum) throws SQLException {
            BlankListRes.Blank entity = new BlankListRes.Blank();
            populateField(rs, entity::setId, "b_id", Integer.class);
            populateField(rs, entity::setNumber, "b_number", String.class);
            populateField(rs, entity::setMaterial, "b_material", String.class);
            populateField(rs, entity::setParams, "b_params", String.class);
            populateField(rs, entity::setCreatedAt, "b_created_at", OffsetDateTime.class);
            populateField(rs, entity::setUpdatedAt, "b_updated_at", OffsetDateTime.class);
            return entity;
        }
    }
}