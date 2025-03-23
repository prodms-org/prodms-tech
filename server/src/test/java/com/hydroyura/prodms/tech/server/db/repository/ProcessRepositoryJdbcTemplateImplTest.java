package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_BLANK_GET_ID_BY_NUMBER;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_BLANK_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_COMMON_TRUNCATE;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_PROCESS_GET_ID_BY_NUMBER;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_PROCESS_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.extractIdFromExecResult;
import static org.junit.jupiter.api.Assertions.*;

import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.ProcessCreateReq;
import com.hydroyura.prodms.tech.server.exception.InsertBlankException;
import java.util.List;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class ProcessRepositoryJdbcTemplateImplTest {

    @ClassRule
    public static PostgreSQLContainer<?> TEST_DB_CONTAINER =
        new PostgreSQLContainer(DockerImageName.parse("postgres:14"))
            .withDatabaseName("test-tech")
            .withPassword("test-pg-pwd")
            .withUsername("test-pg-user");

    @DynamicPropertySource
    public static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add(
            "db.connection.string",
            () -> TEST_DB_CONTAINER.getJdbcUrl() + "&password=test-pg-pwd&user=test-pg-user");
    }

    @BeforeAll
    static void startContainer() {
        TEST_DB_CONTAINER.setPortBindings(List.of("5435:5432"));
        TEST_DB_CONTAINER.start();
    }

    @AfterAll
    static void closeContainer() {
        TEST_DB_CONTAINER.close();
    }

    @AfterEach
    void clearTable() throws Exception {
        TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_COMMON_TRUNCATE.formatted("blanks")
        );
        TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_COMMON_TRUNCATE.formatted("processes")
        );
    }

    @Autowired
    private ProcessRepository repository;


    @Test
    void getByNumber__OK() throws Exception {
        // given
        // -- insert blank
        var resultBlankInsertion = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_BLANK_INSERT_NEW.formatted("BLANK_NUMBER", "BLANK_MATERIAL_NUMBER")
        );
        Integer blankId = extractIdFromExecResult(resultBlankInsertion);

        // -- insert process with blank
        String number = "PROCESS_NUMBER_TEST_1";
        String unit = "PROCESS_UNIT_TEST_1";
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_PROCESS_INSERT_NEW.formatted(number, unit, 1, blankId)
        );

        // when
        var result = repository.get(number);

        // then
        assertTrue(result.isPresent() && result.get().getNumber().equals(number));
    }

    @Test
    void getByNumber__NOT_FOUND() {
        // when
        var result = repository.get("PROCESS_NUMBER_TEST_1");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void create__OK() throws Exception {
        // given

        // -- insert blank
        var resultBlankInsertion = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_BLANK_INSERT_NEW.formatted("BLANK_NUMBER", "BLANK_MATERIAL_NUMBER")
        );
        Integer blankId = extractIdFromExecResult(resultBlankInsertion);

        // -- create request
        ProcessCreateReq req = new ProcessCreateReq();
        req.setNumber("PROCESS_NUMBER_1");
        req.setUnit("PROCESS_UNIT_1");
        req.setPriority(1);
        req.setBlankId(blankId);
        Integer id = repository.create(req);

        // when
        var execResult = TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_PROCESS_GET_ID_BY_NUMBER.formatted(req.getNumber())
        );
        Integer idFromExec = extractIdFromExecResult(execResult);

        // then
        assertEquals(id, idFromExec);
    }

    @Test
    void create__DUBLICATION() throws Exception {
        // given
        // -- insert blank
        var resultBlankInsertion = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_BLANK_INSERT_NEW.formatted("BLANK_NUMBER", "BLANK_MATERIAL_NUMBER")
        );
        Integer blankId = extractIdFromExecResult(resultBlankInsertion);

        // -- create request
        ProcessCreateReq req = new ProcessCreateReq();
        req.setNumber("PROCESS_NUMBER_1");
        req.setUnit("PROCESS_UNIT_1");
        req.setPriority(1);
        req.setBlankId(blankId);
        repository.create(req);

        // when
        Executable executable = () -> repository.create(req);

        // then
        assertThrows(InsertBlankException.class, executable);
    }

    @Test
    void create__BAD_DATA__NON_EXISTENT_BLANK_ID() {

    }


}