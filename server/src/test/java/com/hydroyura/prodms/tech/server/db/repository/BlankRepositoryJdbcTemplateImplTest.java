package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_BLANK_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_COMMON_TRUNCATE;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_PROCESS_INSERT_NEW;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
import java.util.List;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
class BlankRepositoryJdbcTemplateImplTest {

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
    private BlankRepository blankRepository;


    @Test
    void getByNumber__WITHOUT_PROCESSES__OK() throws Exception {
        // given
        String number = "BLANK_NUMBER_TEST_1";
        String material = "BLANK_MATERIAL_TEST_1";
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_BLANK_INSERT_NEW.formatted(number, material)
        );

        // when
        var result = blankRepository.get(number);

        // then
        assertTrue(result.isPresent() && result.get().getNumber().equals(number));
    }

    @Test
    void getByNumber__WITH_TWO_PROCESSES__OK() throws Exception {
        // given
        // -- add blank
        String number = "BLANK_NUMBER_TEST_1";
        String material = "BLANK_MATERIAL_TEST_1";
        var execBlankResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_BLANK_INSERT_NEW.formatted(number, material)
        );
        Integer blankId = Integer.valueOf(
            execBlankResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add process #1
        String processNumber1 = "PROCESS_NUMBER_1";
        String processUnit1 = "PROCESS_UNIT_1";
        var execProcessResult1 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_PROCESS_INSERT_NEW.formatted(processNumber1, processUnit1, 0, blankId)
        );

        // TODO: extract into separate static method
        Integer processId1 = Integer.valueOf(
            execProcessResult1.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add process #2
        String processNumber2 = "PROCESS_NUMBER_2";
        String processUnit2 = "PROCESS_UNIT_2";
        var execProcessResult2 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_PROCESS_INSERT_NEW.formatted(processNumber2, processUnit2, 0, blankId)
        );

        // TODO: extract into separate static method
        Integer processId2 = Integer.valueOf(
            execProcessResult2.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );

        // when
        var result = blankRepository.get(number);

        // then
        assertTrue(result.isPresent()
            && result.get().getId().equals(blankId)
            && result.get().getProcesses().size() == 2
            && result.get().getProcesses()
                .stream()
                .map(SingleBlankRes.Process::getId)
                .toList()
                .containsAll(List.of(processId1, processId2))
        );
    }

    @Test
    void getByNumber__NOT_FOUND() {
        // when
        var result = blankRepository.get("BLANK_NUMBER_TEST_1");

        // then
        assertTrue(result.isEmpty());
    }
}