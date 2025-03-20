package com.hydroyura.prodms.tech.server.db.repository;

import org.testcontainers.containers.Container;

public class RepositoryTestUtils {

    public static Integer extractIdFromExecResult(Container.ExecResult execResult) {
        return Integer.valueOf(execResult.getStdout().split("\n")[2].replace(" ", ""));
    }

    public static final String SQL_COMMON_TRUNCATE =
        "echo \"TRUNCATE TABLE %s CASCADE;\" | psql -U test-pg-user -d test-tech";


    public static final String SQL_EQ_INSERT_NEW = """
        echo "
            INSERT INTO equipments (number, name, type, created_at, updated_at) \s
            VALUES('%s', '%s', %s, now(), now()) \s
            RETURNING id;" | psql -U test-pg-user -d test-tech
        \s
    """;

    public static final String SQL_EQ_GET_ID_BY_NUMBER = """
        echo "
            SELECT id FROM equipments \s
            WHERE number = '%s'" | psql -U test-pg-user -d test-tech
        \s
    """;

    public static final String SQL_EQ_SET_GET_ID_BY_NUMBER = """
        echo "
            SELECT id FROM equipments_sets \s
            WHERE number = '%s'" | psql -U test-pg-user -d test-tech
        \s
    """;

    public static final String SQL_BLANK_GET_ID_BY_NUMBER = """
        echo "
            SELECT id FROM blanks \s
            WHERE number = '%s'" | psql -U test-pg-user -d test-tech
        \s
    """;


    public static final String SQL_EQ_SET_INSERT_NEW = """
        echo "
            INSERT INTO equipments_sets (number, name, description, created_at, updated_at) \s
            VALUES('%s', '%s', '%s', now(), now()) \s
            RETURNING id;" | psql -U test-pg-user -d test-tech
        \s
    """;



    public static final String SQL_EQ_SET_COMP_INSERT_NEW = """
        echo "
            INSERT INTO equipments_sets_composition (equipment_id, set_id, created_at, updated_at) \s
            VALUES(%s, %s, now(), now());" | psql -U test-pg-user -d test-tech
        \s
    """;

    public static final String SQL_BLANK_INSERT_NEW = """
        echo "
            INSERT INTO blanks (number, material, params, created_at, updated_at) \s
            VALUES('%s', '%s', '{\\"name\\": \\"test-name\\", \\"age\\": 17}', now(), now()) \s
            RETURNING id;" | psql -U test-pg-user -d test-tech
        \s
    """;

    public static final String SQL_PROCESS_INSERT_NEW = """
        echo "
            INSERT INTO processes (number, unit, created_at, updated_at, priority, blank_id) \s
            VALUES('%s', '%s', now(), now(), %s, %s) \s
            RETURNING id;" | psql -U test-pg-user -d test-tech
        \s
    """;

}