package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.populateField;

import com.hydroyura.prodms.tech.server.exception.InsertEquipmentException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
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
import com.hydroyura.prodms.tech.client.req.EquipmentCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentRes.EquipmentSet;


@RequiredArgsConstructor
@Slf4j
public class EquipmentRepositoryJdbcTemplateImpl implements EquipmentRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<SingleEquipmentRes> rowMapperSingleEquipment = new SingleEquipmentRowMapper();
    private final RowMapper<EquipmentListRes.Equipment> rowMapperEquipmentList = new EquipmentListRowMapper();

    private static final String LOG_MSG_CANNOT_INSERT_EQUIPMENT = """
            Can not insert new equipment with values: number = [%s], name = [%s], type = [%s]
        """;

    // TODO: remove duplication
    @Override
    public EquipmentListRes list(EquipmentListReq filter) {
        String nativeBaseQueryCount = """
            SELECT COUNT(*) FROM equipments eq
            """;

        List<Predicate> predicates = new ArrayList<>();

        Optional
            .ofNullable(filter.getNumberLike())
            .map(v -> new Predicate("eq.number", v, Operation.LIKE))
            .ifPresent(predicates::add);
        Optional
            .ofNullable(filter.getNameLike())
            .map(v -> new Predicate("eq.name", v, Operation.LIKE))
            .ifPresent(predicates::add);

        StringBuilder sbCount = new StringBuilder(nativeBaseQueryCount);
        if (!predicates.isEmpty()) {
            sbCount.append(" WHERE ");
            String countCondition = predicates
                .stream()
                .map(Predicate::buildCondition)
                .collect(Collectors.joining(" AND "));
            sbCount.append(countCondition);
        }
        String nativeQueryCount = sbCount.toString();
        Integer totalCount = jdbcTemplate.queryForObject(nativeQueryCount, Integer.class);

        String nativeBaseQueryValues = """
            SELECT eq.id AS eq_id, eq.number AS eq_number, eq.name AS eq_name, eq.type AS eq_type,
                   eq.created_at AS eq_created_at, eq.updated_at AS eq_updated_at
               FROM equipments eq
            """;
        StringBuilder sbValues = new StringBuilder(nativeBaseQueryValues);
        if (!predicates.isEmpty()) {
            sbValues.append(" WHERE ");
            String countCondition = predicates
                .stream()
                .map(Predicate::buildCondition)
                .collect(Collectors.joining(" AND "));
            sbValues.append(countCondition);
        }
        String pagination = buildPagination(filter);
        sbValues.append(pagination);
        String nativeQueryValues = sbValues.toString();
        List<EquipmentListRes.Equipment> values = jdbcTemplate.query(nativeQueryValues, rowMapperEquipmentList);

        EquipmentListRes response = new EquipmentListRes();
        response.setEquipments(values);
        response.setTotalCount(totalCount);
        return response;
    }

    private String buildPagination(EquipmentListReq req) {
        return " ORDER BY " + req.getSortCode().getField() + " " + req.getSortCode().getDirection() +
            " LIMIT " + req.getItemsPerPage() + " OFFSET " + req.getPage() * req.getItemsPerPage();
    }

    @AllArgsConstructor
    private static class Predicate {
        private String field;
        private String value;
        private Operation operation;

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
    public Integer create(EquipmentCreateReq equipment) {
        String nativeQuery = """
            INSERT INTO equipments (number, name, type, created_at, updated_at)
                VALUES (:number, :name, :type, now(), now())
                RETURNING id
            """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("number", equipment.getNumber())
            .addValue("name", equipment.getName())
            .addValue("type", equipment.getType());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(nativeQuery, params, keyHolder);
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage(), e);
            throw new InsertEquipmentException(
                LOG_MSG_CANNOT_INSERT_EQUIPMENT.formatted(
                    equipment.getNumber(),
                    equipment.getName(),
                    equipment.getType()),
                e);
        }
    }

    @Override
    public Optional<SingleEquipmentRes> get(Integer id) {

        String nativeQuery = """
            SELECT eq.id AS eq_id, eq.number AS eq_number, eq.name AS eq_name, eq.type AS eq_type,
                   eq.created_at AS eq_created_at, eq.updated_at AS eq_updated_at,
                   eq_sets.id AS eq_sets_id, eq_sets.description AS eq_sets_description,
                   eq_sets.number AS eq_sets_number, eq_sets.name AS eq_sets_name,
                   eq_sets.created_at AS eq_sets_created_at, eq_sets.updated_at AS eq_sets_updated_at
                FROM equipments eq
                    LEFT JOIN equipments_sets_composition eq_sets_comp
                        ON eq.id = eq_sets_comp.equipment_id
                    LEFT JOIN equipments_sets eq_sets
                        ON eq_sets.id = eq_sets_comp.set_id
                WHERE eq.id = :id
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        List<SingleEquipmentRes> resultList = namedParameterJdbcTemplate.query(nativeQuery, params,
            rowMapperSingleEquipment);
        return resultList.stream().findFirst();
    }

    @Override
    public Optional<SingleEquipmentRes> get(String number) {
        String nativeQuery = """
            SELECT eq.id AS eq_id, eq.number AS eq_number, eq.name AS eq_name, eq.type AS eq_type,
                   eq.created_at AS eq_created_at, eq.updated_at AS eq_updated_at,
                   eq_sets.id AS eq_sets_id, eq_sets.description AS eq_sets_description,
                   eq_sets.number AS eq_sets_number, eq_sets.name AS eq_sets_name,
                   eq_sets.created_at AS eq_sets_created_at, eq_sets.updated_at AS eq_sets_updated_at
                FROM equipments eq
                    LEFT JOIN equipments_sets_composition eq_sets_comp
                        ON eq.id = eq_sets_comp.equipment_id
                    LEFT JOIN equipments_sets eq_sets
                        ON eq_sets.id = eq_sets_comp.set_id
                WHERE eq.number = :number
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("number", number);
        List<SingleEquipmentRes> resultList = namedParameterJdbcTemplate.query(nativeQuery, params,
            rowMapperSingleEquipment);
        return resultList.stream().findFirst();
    }

    private static class SingleEquipmentRowMapper implements RowMapper<SingleEquipmentRes> {

        @Override
        public SingleEquipmentRes mapRow(ResultSet rs, int rowNum) throws SQLException {
            SingleEquipmentRes equipment = new SingleEquipmentRes();
            populateField(rs, equipment::setId, "eq_id", Integer.class);
            populateField(rs, equipment::setName, "eq_name", String.class);
            populateField(rs, equipment::setNumber, "eq_number", String.class);
            populateField(rs, equipment::setType, "eq_type", Integer.class);
            populateField(rs, equipment::setCreatedAt, "eq_created_at", OffsetDateTime.class);
            populateField(rs, equipment::setUpdatedAt, "eq_updated_at", OffsetDateTime.class);

            Set<EquipmentSet> sets = new HashSet<>();
            do {
                if (Objects.nonNull(rs.getObject("eq_sets_id", Integer.class))) {
                    EquipmentSet set = new EquipmentSet();
                    populateField(rs, set::setId, "eq_sets_id", Integer.class);
                    populateField(rs, set::setDescription, "eq_sets_description", String.class);
                    populateField(rs, set::setNumber, "eq_sets_number", String.class);
                    populateField(rs, set::setName, "eq_sets_name", String.class);
                    populateField(rs, set::setCreatedAt, "eq_sets_created_at", OffsetDateTime.class);
                    populateField(rs, set::setUpdatedAt, "eq_sets_updated_at", OffsetDateTime.class);
                    sets.add(set);
                }
            } while (rs.next());

            equipment.setSets(sets);
            return equipment;
        }
    }

    private static class EquipmentListRowMapper implements RowMapper<EquipmentListRes.Equipment> {

        @Override
        public EquipmentListRes.Equipment mapRow(ResultSet rs, int rowNum) throws SQLException {
            EquipmentListRes.Equipment equipment = new EquipmentListRes.Equipment();
            populateField(rs, equipment::setId, "eq_id", Integer.class);
            populateField(rs, equipment::setName, "eq_name", String.class);
            populateField(rs, equipment::setNumber, "eq_number", String.class);
            populateField(rs, equipment::setType, "eq_type", Integer.class);
            populateField(rs, equipment::setCreatedAt, "eq_created_at", OffsetDateTime.class);
            populateField(rs, equipment::setUpdatedAt, "eq_updated_at", OffsetDateTime.class);
            return equipment;
        }
    }
}