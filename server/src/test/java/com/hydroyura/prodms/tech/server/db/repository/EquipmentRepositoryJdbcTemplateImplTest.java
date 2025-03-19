package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_COMMON_TRUNCATE;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_GET_ID_BY_NUMBER;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_SET_COMP_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_SET_INSERT_NEW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hydroyura.prodms.tech.client.enums.EquipmentSortCode;
import com.hydroyura.prodms.tech.client.req.EquipmentCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentListReq;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentRes;
import com.hydroyura.prodms.tech.server.exception.InsertEquipmentException;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
class EquipmentRepositoryJdbcTemplateImplTest {

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
            SQL_COMMON_TRUNCATE.formatted("equipments")
        );
        TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_COMMON_TRUNCATE.formatted("equipments_sets")
        );
        TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_COMMON_TRUNCATE.formatted("equipments_sets_composition")
        );
    }

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    void getById__WITHOUT_EQ_SETS__OK() throws Exception {
        // given
        String number = "EQ_NUMBER_TEST_1";
        String name = "EQ_NAME_TEST_1";
        var execResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(number, name, 1)
        );
        Integer id = Integer.valueOf(
            execResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );

        // when
        var result = equipmentRepository.get(id);

        // then
        assertTrue(result.isPresent() && result.get().getId().equals(id));
    }

    @Test
    void getById__WITH_TWO_EQ_SETS__OK() throws Exception {
        // given
        // -- add Equipment
        String eqNumber = "EQ_NUMBER_TEST_1";
        String eqName = "EQ_NAME_TEST_1";
        var execEqResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(eqNumber, eqName, 1)
        );
        Integer eqId = Integer.valueOf(
            execEqResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment set #1
        String eqSetNumber1 = "EQ_SET_NUMBER_1";
        String eqSetName1 = "EQ_SET_NAME_1";
        String eqSetDescription1 = "EQ_SET_DESCRIPTION_1";
        var execEqSetResult1 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(eqSetNumber1, eqSetName1, eqSetDescription1)
        );
        Integer eqSetId1 = Integer.valueOf(
            execEqSetResult1.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment set #2
        String eqSetNumber2 = "EQ_SET_NUMBER_2";
        String eqSetName2 = "EQ_SET_NAME_2";
        String eqSetDescription2 = "EQ_SET_DESCRIPTION_2";
        var execEqSetResult2 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(eqSetNumber2, eqSetName2, eqSetDescription2)
        );
        Integer eqSetId2 = Integer.valueOf(
            execEqSetResult2.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add relation equipment#1 and set_equipment#1
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId, eqSetId1)
        );
        // -- add relation equipment#1 and set_equipment#2
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId, eqSetId2)
        );

        // when
        var result = equipmentRepository.get(eqId);

        // then
        assertTrue(result.isPresent()
            && result.get().getId().equals(eqId)
            && result.get().getSets().size() == 2
            && result.get().getSets()
                .stream()
                .map(SingleEquipmentRes.EquipmentSet::getId)
                .toList()
                .containsAll(List.of(eqSetId1, eqSetId2))
        );
    }

    @Test
    void getById__NOT_FOUND() {
        // when
        var result = equipmentRepository.get(1);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getByNumber__WITHOUT_EQ_SETS__OK() throws Exception {
        // given
        String number = "EQ_NUMBER_TEST_1";
        String name = "EQ_NAME_TEST_1";
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(number, name, 1)
        );

        // when
        var result = equipmentRepository.get(number);

        // then
        assertTrue(result.isPresent() && result.get().getNumber().equals(number));
    }

    @Test
    void getByNumber__WITH_TWO_EQ_SETS__OK() throws Exception {
        // given
        // -- add Equipment
        String eqNumber = "EQ_NUMBER_TEST_1";
        String eqName = "EQ_NAME_TEST_1";
        var execEqResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(eqNumber, eqName, 1)
        );
        Integer eqId = Integer.valueOf(
            execEqResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment set #1
        String eqSetNumber1 = "EQ_SET_NUMBER_1";
        String eqSetName1 = "EQ_SET_NAME_1";
        String eqSetDescription1 = "EQ_SET_DESCRIPTION_1";
        var execEqSetResult1 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(eqSetNumber1, eqSetName1, eqSetDescription1)
        );
        Integer eqSetId1 = Integer.valueOf(
            execEqSetResult1.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment set #2
        String eqSetNumber2 = "EQ_SET_NUMBER_2";
        String eqSetName2 = "EQ_SET_NAME_2";
        String eqSetDescription2 = "EQ_SET_DESCRIPTION_2";
        var execEqSetResult2 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(eqSetNumber2, eqSetName2, eqSetDescription2)
        );
        Integer eqSetId2 = Integer.valueOf(
            execEqSetResult2.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add relation equipment#1 and set_equipment#1
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId, eqSetId1)
        );
        // -- add relation equipment#1 and set_equipment#2
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId, eqSetId2)
        );

        // when
        var result = equipmentRepository.get(eqNumber);

        // then
        assertTrue(result.isPresent()
            && result.get().getId().equals(eqId)
            && result.get().getSets().size() == 2
            && result.get().getSets()
            .stream()
            .map(SingleEquipmentRes.EquipmentSet::getId)
            .toList()
            .containsAll(List.of(eqSetId1, eqSetId2))
        );
    }

    @Test
    void getByNumber__NOT_FOUND() {
        // when
        var result = equipmentRepository.get("EQ_NUMBER_TEST_1");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void create__OK() throws Exception {
        // given
        EquipmentCreateReq req = new EquipmentCreateReq();
        req.setName("EQ_NAME");
        req.setNumber("EQ_NUMBER");
        req.setType(1);
        var id = equipmentRepository.create(req);

        // when
        var execResult = TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_EQ_GET_ID_BY_NUMBER.formatted(req.getNumber())
        );
        Integer idFromExec = Integer.valueOf(
            execResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );

        // then
        assertEquals(id, idFromExec);
    }

    @Test
    void create__DUBLICATION() throws Exception {
        // given
        EquipmentCreateReq req = new EquipmentCreateReq();
        req.setName("EQ_NAME");
        req.setNumber("EQ_NUMBER");
        req.setType(1);
        equipmentRepository.create(req);

        // when
        Executable executable = () -> equipmentRepository.create(req);

        // then
        assertThrows(InsertEquipmentException.class, executable);
    }

    @Test
    void list__OK() throws Exception {
        // given
        EquipmentListReq filter = new EquipmentListReq();
        filter.setItemsPerPage(10);
        filter.setPage(2);
        filter.setSortCode(EquipmentSortCode.NAME_ASC);

        int total = 100;
        for (int i = 1; i <= total ; i++) {
            String nameIndex = (total + 1 - i) <= 9 ? "0" + (total + 1 - i) : String.valueOf(total + 1 - i);
            String name = "EQUIPMENT_NAME__" + nameIndex;

            String numberIndex = i <= 9 ? "0" + i : String.valueOf(i);
            String number = "EQUIPMENT_NUMBER__" + numberIndex;

            TEST_DB_CONTAINER.execInContainer("bash", "-c",
                SQL_EQ_INSERT_NEW.formatted(number, name, 1)
            );

        }

        // when
        var result = equipmentRepository.list(filter);

        // then
        // -- check total count
        assertEquals(total, result.getTotalCount());
        // -- check current count
        assertEquals(filter.getItemsPerPage(), result.getEquipments().size());
        // -- check offset && sort
        Integer indexStart = Integer.valueOf(result.getEquipments()
            .stream()
            .toList()
            .get(0).getName().split("__")[1]);
        assertEquals(filter.getItemsPerPage() * filter.getPage(), indexStart);
        Integer indexEnd = Integer.valueOf(result.getEquipments()
            .stream()
            .toList()
            .get(result.getEquipments().size() - 1).getName().split("__")[1]);
        assertEquals(filter.getItemsPerPage() * filter.getPage() + filter.getItemsPerPage() - 1, indexEnd);

    }

}