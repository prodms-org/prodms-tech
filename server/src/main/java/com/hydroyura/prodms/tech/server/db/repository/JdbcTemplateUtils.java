package com.hydroyura.prodms.tech.server.db.repository;

import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import java.sql.ResultSet;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class JdbcTemplateUtils {

    private static final String LOG_MSG_COLUMN_NOT_EXIST = "Column with name = [{}] doesn't present in resultSet";

    static <T> void populateField(ResultSet rs, Consumer<T> consumer, String column, Class<T> valueType) {
        T value = null;
        try {
            value = rs.getObject(column, valueType);
        } catch (Exception e) {
            log.error(LOG_MSG_COLUMN_NOT_EXIST, column, e);
        }
        Optional
            .ofNullable(value)
            .ifPresent(consumer);
    }

    static String buildSortAndPagination(String sortField, String sortDirection, int itemsPerPage, int offset) {
        return " ORDER BY %s %s LIMIT %s OFFSET %s".formatted(sortField, sortDirection, itemsPerPage, offset);
    }


}
