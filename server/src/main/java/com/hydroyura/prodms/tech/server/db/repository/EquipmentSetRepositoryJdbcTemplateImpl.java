package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.populateField;
import static com.hydroyura.prodms.tech.server.db.repository.JdbcTemplateUtils.buildSortAndPagination;

import com.hydroyura.prodms.tech.client.req.EquipmentSetAddEquipmentsReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import com.hydroyura.prodms.tech.client.res.EquipmentSetAddEquipmentsRes;
import com.hydroyura.prodms.tech.client.res.EquipmentSetListRes;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentSetRes;
import com.hydroyura.prodms.tech.server.exception.InsertEquipmentSetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class EquipmentSetRepositoryJdbcTemplateImpl implements EquipmentSetRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SingleEquipmentSetRes> rowMapperSingleEquipmentSet = new SingleEquipmentSetRowMapper();
    private final RowMapper<EquipmentSetListRes.EquipmentSet> rowMapperEquipmentSetList = new EquipmentSetListRowMapper();

    private static final String LOG_MSG_CANNOT_INSERT_EQUIPMENT_SET = """
            Can not insert new equipment-set with values: number = [%s], name = [%s], description = [%s]
        """;

    @Override
    public Integer create(EquipmentSetCreateReq equipmentSet) {
        String nativeQuery = """
            INSERT INTO equipments_sets (number, name, description, created_at, updated_at)
                VALUES (:number, :name, :description, now(), now())
                RETURNING id
        """;

        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("number", equipmentSet.getNumber())
            .addValue("name", equipmentSet.getName())
            .addValue("description", equipmentSet.getDescription());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            namedParameterJdbcTemplate.update(nativeQuery, params, keyHolder);
            return Objects.requireNonNull(keyHolder.getKey()).intValue();
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage(), e);
            throw new InsertEquipmentSetException(
                LOG_MSG_CANNOT_INSERT_EQUIPMENT_SET.formatted(
                    equipmentSet.getNumber(),
                    equipmentSet.getName(),
                    equipmentSet.getDescription()),
                e);
        }
    }

    // TODO: remove duplication
    @Override
    public EquipmentSetListRes list(EquipmentSetListReq filter) {
        String nativeBaseQueryCount = """
            SELECT COUNT(*) FROM equipments_sets eq_set
            """;

        List<EquipmentSetRepositoryJdbcTemplateImpl.Predicate> predicates = new ArrayList<>();

        Optional
            .ofNullable(filter.getNumberLike())
            .map(v -> new EquipmentSetRepositoryJdbcTemplateImpl.Predicate("eq.number", v, EquipmentSetRepositoryJdbcTemplateImpl.Operation.LIKE))
            .ifPresent(predicates::add);
        Optional
            .ofNullable(filter.getNameLike())
            .map(v -> new EquipmentSetRepositoryJdbcTemplateImpl.Predicate("eq.name", v, EquipmentSetRepositoryJdbcTemplateImpl.Operation.LIKE))
            .ifPresent(predicates::add);

        StringBuilder sbCount = new StringBuilder(nativeBaseQueryCount);
        if (!predicates.isEmpty()) {
            sbCount.append(" WHERE ");
            String countCondition = predicates
                .stream()
                .map(EquipmentSetRepositoryJdbcTemplateImpl.Predicate::buildCondition)
                .collect(Collectors.joining(" AND "));
            sbCount.append(countCondition);
        }
        String nativeQueryCount = sbCount.toString();
        Integer totalCount = jdbcTemplate.queryForObject(nativeQueryCount, Integer.class);

        String nativeBaseQueryValues = """
            SELECT eq_sets.id AS eq_sets_id, eq_sets.description AS eq_sets_description,
                   eq_sets.number AS eq_sets_number, eq_sets.name AS eq_sets_name,
                   eq_sets.created_at AS eq_sets_created_at, eq_sets.updated_at AS eq_sets_updated_at
               FROM equipments_sets eq_sets
            """;
        StringBuilder sbValues = new StringBuilder(nativeBaseQueryValues);
        if (!predicates.isEmpty()) {
            sbValues.append(" WHERE ");
            String countCondition = predicates
                .stream()
                .map(EquipmentSetRepositoryJdbcTemplateImpl.Predicate::buildCondition)
                .collect(Collectors.joining(" AND "));
            sbValues.append(countCondition);
        }
        String pagination = buildPagination(filter);
        sbValues.append(pagination);
        String nativeQueryValues = sbValues.toString();
        List<EquipmentSetListRes.EquipmentSet> values = jdbcTemplate.query(nativeQueryValues, rowMapperEquipmentSetList);

        EquipmentSetListRes response = new EquipmentSetListRes();
        response.setSets(values);
        response.setTotalCount(totalCount);
        return response;
    }

    private String buildPagination(EquipmentSetListReq req) {
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
        private EquipmentSetRepositoryJdbcTemplateImpl.Operation operation;

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
    public Optional<SingleEquipmentSetRes> get(Integer id) {

        String nativeQuery = """
            SELECT eq_sets.id AS eq_sets_id, eq_sets.description AS eq_sets_description,
                   eq_sets.number AS eq_sets_number, eq_sets.name AS eq_sets_name,
                   eq_sets.created_at AS eq_sets_created_at, eq_sets.updated_at AS eq_sets_updated_at,
                   eq.id AS eq_id, eq.number AS eq_number, eq.name AS eq_name, eq.type AS eq_type,
                   eq.created_at AS eq_created_at, eq.updated_at AS eq_updated_at
                FROM equipments_sets eq_sets
                    LEFT JOIN equipments_sets_composition eq_sets_comp
                        ON eq_sets.id = eq_sets_comp.set_id
                    LEFT JOIN equipments eq
                        ON eq.id = eq_sets_comp.equipment_id
                WHERE eq_sets.id = :id
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        List<SingleEquipmentSetRes> resultList = namedParameterJdbcTemplate.query(
            nativeQuery,
            params,
            rowMapperSingleEquipmentSet
        );
        return resultList.stream().findFirst();
    }

    @Override
    public Optional<SingleEquipmentSetRes> get(String number) {
        String nativeQuery = """
            SELECT eq_sets.id AS eq_sets_id, eq_sets.description AS eq_sets_description,
                   eq_sets.number AS eq_sets_number, eq_sets.name AS eq_sets_name,
                   eq_sets.created_at AS eq_sets_created_at, eq_sets.updated_at AS eq_sets_updated_at,
                   eq.id AS eq_id, eq.number AS eq_number, eq.name AS eq_name, eq.type AS eq_type,
                   eq.created_at AS eq_created_at, eq.updated_at AS eq_updated_at
                FROM equipments_sets eq_sets
                    LEFT JOIN equipments_sets_composition eq_sets_comp
                        ON eq_sets.id = eq_sets_comp.set_id
                    LEFT JOIN equipments eq
                        ON eq.id = eq_sets_comp.equipment_id
                WHERE eq_sets.number = :number
            """;

        SqlParameterSource params = new MapSqlParameterSource().addValue("number", number);
        List<SingleEquipmentSetRes> resultList = namedParameterJdbcTemplate.query(
            nativeQuery,
            params,
            rowMapperSingleEquipmentSet
        );
        return resultList.stream().findFirst();
    }


    @Override
    @Transactional
    public EquipmentSetAddEquipmentsRes addEquipments(String number, EquipmentSetAddEquipmentsReq equipments) {
        String nativeQueryFetchEquipmentIds = """
            SELECT eq.id AS eq_id, eq.number AS eq_number FROM equipments eq WHERE eq.number IN (:numbers)
        """;
        SqlParameterSource paramsFetchEquipmentsIds = new MapSqlParameterSource()
            .addValue("numbers", equipments.getNumbers());
        List<Map<String, Object>> existedEquipments = namedParameterJdbcTemplate.queryForList(nativeQueryFetchEquipmentIds, paramsFetchEquipmentsIds);
        List<Integer> existedEquipmentIds = existedEquipments
            .stream()
            .map(v -> (Integer) v.get("eq_id"))
            .toList();

        String nativeQueryFetchSetId = """
            SELECT eq_set.id AS id FROM equipments_sets eq_set WHERE eq_set.number = :number;
        """;
        SqlParameterSource paramsFetchEquipmentSetId = new MapSqlParameterSource()
            .addValue("number", number);
        Integer setId = namedParameterJdbcTemplate.queryForObject(nativeQueryFetchSetId, paramsFetchEquipmentSetId, Integer.class);
        // TODO: add missing set handling

        List<Object[]> batchValues = existedEquipmentIds
            .stream()
            .map(id -> new Object[]{id, setId})
            .toList();

        String nativeQueryInsert = """
            INSERT INTO equipments_sets_composition (equipment_id, set_id, created_at, updated_at)
                VALUES (?, ?, now(), now())
        """;
        int[] result = jdbcTemplate.batchUpdate(nativeQueryInsert, batchValues);
        Integer totalInsertedCount = Arrays.stream(result).sum();

        EquipmentSetAddEquipmentsRes res = new EquipmentSetAddEquipmentsRes();
        res.setTotalInsertedCount(totalInsertedCount);
        res.setExistedEquipmentIds(existedEquipmentIds);

        return res;
    }

    private static class SingleEquipmentSetRowMapper implements RowMapper<SingleEquipmentSetRes> {

        @Override
        public SingleEquipmentSetRes mapRow(ResultSet rs, int rowNum) throws SQLException {
            SingleEquipmentSetRes equipmentSet = new SingleEquipmentSetRes();
            populateField(rs, equipmentSet::setId, "eq_sets_id", Integer.class);
            populateField(rs, equipmentSet::setDescription, "eq_sets_description", String.class);
            populateField(rs, equipmentSet::setNumber, "eq_sets_number", String.class);
            populateField(rs, equipmentSet::setName, "eq_sets_name", String.class);
            populateField(rs, equipmentSet::setCreatedAt, "eq_sets_created_at", OffsetDateTime.class);
            populateField(rs, equipmentSet::setUpdatedAt, "eq_sets_updated_at", OffsetDateTime.class);
            Set<SingleEquipmentSetRes.Equipment> equipments = new HashSet<>();
            do {
                if (Objects.nonNull(rs.getObject("eq_sets_id", Integer.class))) {
                    SingleEquipmentSetRes.Equipment equipment = new SingleEquipmentSetRes.Equipment();
                    populateField(rs, equipment::setId, "eq_id", Integer.class);
                    populateField(rs, equipment::setName, "eq_name", String.class);
                    populateField(rs, equipment::setNumber, "eq_number", String.class);
                    populateField(rs, equipment::setType, "eq_type", Integer.class);
                    populateField(rs, equipment::setCreatedAt, "eq_created_at", OffsetDateTime.class);
                    populateField(rs, equipment::setUpdatedAt, "eq_updated_at", OffsetDateTime.class);
                    equipments.add(equipment);
                }
            } while (rs.next());

            equipmentSet.setEquipments(equipments);
            return equipmentSet;
        }
    }

    private static class EquipmentSetListRowMapper implements RowMapper<EquipmentSetListRes.EquipmentSet> {

        @Override
        public EquipmentSetListRes.EquipmentSet mapRow(ResultSet rs, int rowNum) throws SQLException {
            EquipmentSetListRes.EquipmentSet equipmentSet = new EquipmentSetListRes.EquipmentSet();
            populateField(rs, equipmentSet::setId, "eq_sets_id", Integer.class);
            populateField(rs, equipmentSet::setDescription, "eq_sets_description", String.class);
            populateField(rs, equipmentSet::setNumber, "eq_sets_number", String.class);
            populateField(rs, equipmentSet::setName, "eq_sets_name", String.class);
            populateField(rs, equipmentSet::setCreatedAt, "eq_sets_created_at", OffsetDateTime.class);
            populateField(rs, equipmentSet::setUpdatedAt, "eq_sets_updated_at", OffsetDateTime.class);
            return equipmentSet;
        }
    }
}
