package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_BLANK_GET_ID_BY_NUMBER;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_BLANK_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_COMMON_TRUNCATE;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_SET_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_PROCESS_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.extractIdFromExecResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hydroyura.prodms.tech.client.enums.BlankSortCode;
import com.hydroyura.prodms.tech.client.req.BlankCreateReq;
import com.hydroyura.prodms.tech.client.req.BlankListReq;
import com.hydroyura.prodms.tech.client.res.SingleBlankRes;
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
        Integer processId1 = extractIdFromExecResult(execProcessResult1);

        // -- add process #2
        String processNumber2 = "PROCESS_NUMBER_2";
        String processUnit2 = "PROCESS_UNIT_2";
        var execProcessResult2 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_PROCESS_INSERT_NEW.formatted(processNumber2, processUnit2, 0, blankId)
        );
        Integer processId2 = extractIdFromExecResult(execProcessResult2);

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

    @Test
    void create__OK() throws Exception {
        // given
        BlankCreateReq req = new BlankCreateReq();
        req.setNumber("BLANK_NUMBER_1");
        req.setMaterial("BLANK_MATERIAL_1");
        var id = blankRepository.create(req);

        // when
        var execResult = TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_BLANK_GET_ID_BY_NUMBER.formatted(req.getNumber())
        );
        Integer idFromExec = extractIdFromExecResult(execResult);

        // then
        assertEquals(id, idFromExec);
    }

    @Test
    void create__DUBLICATION() throws Exception {
        // given
        BlankCreateReq req = new BlankCreateReq();
        req.setNumber("BLANK_NUMBER_1");
        req.setMaterial("BLANK_MATERIAL_1");
        blankRepository.create(req);

        // when
        Executable executable = () -> blankRepository.create(req);

        // then
        assertThrows(InsertBlankException.class, executable);
    }

    @Test
    void list__OK() throws Exception {
        // given
        BlankListReq filter = new BlankListReq();
        filter.setItemsPerPage(10);
        filter.setPage(2);
        filter.setSortCode(BlankSortCode.MATERIAL_ASC);

        int total = 100;
        for (int i = 1; i <= total ; i++) {
            String materialIndex = (total + 1 - i) <= 9 ? "0" + (total + 1 - i) : String.valueOf(total + 1 - i);
            String material = "BLANK_MATERIAL__" + materialIndex;

            String numberIndex = i <= 9 ? "0" + i : String.valueOf(i);
            String number = "BLANK_NUMBER__" + numberIndex;

            TEST_DB_CONTAINER.execInContainer("bash", "-c",
                SQL_BLANK_INSERT_NEW.formatted(number, material)
            );

        }

        // when
        var result = blankRepository.list(filter);

        // then
        // -- check total count
        assertEquals(total, result.getTotalCount());
        // -- check current count
        assertEquals(filter.getItemsPerPage(), result.getBlanks().size());
        // -- check offset && sort
        Integer indexStart = Integer.valueOf(result.getBlanks()
            .stream()
            .toList()
            .get(0).getMaterial().split("__")[1]);
        assertEquals(filter.getItemsPerPage() * filter.getPage(), indexStart);
        Integer indexEnd = Integer.valueOf(result.getBlanks()
            .stream()
            .toList()
            .get(result.getBlanks().size() - 1).getMaterial().split("__")[1]);
        assertEquals(filter.getItemsPerPage() * filter.getPage() + filter.getItemsPerPage() - 1, indexEnd);

    }


}