package com.hydroyura.prodms.tech.server.db.repository;

import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_COMMON_TRUNCATE;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_SET_COMP_INSERT_NEW;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_SET_GET_ID_BY_NUMBER;
import static com.hydroyura.prodms.tech.server.db.repository.RepositoryTestUtils.SQL_EQ_SET_INSERT_NEW;
import static org.junit.jupiter.api.Assertions.*;

import com.hydroyura.prodms.tech.client.enums.EquipmentSetSortCode;
import com.hydroyura.prodms.tech.client.req.EquipmentSetAddEquipmentsReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetCreateReq;
import com.hydroyura.prodms.tech.client.req.EquipmentSetListReq;
import com.hydroyura.prodms.tech.client.res.SingleEquipmentSetRes;
import com.hydroyura.prodms.tech.server.exception.InsertEquipmentSetException;
import java.util.ArrayList;
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
@RequiredArgsConstructor
class EquipmentSetRepositoryJdbcTemplateImplTest {


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
    private EquipmentSetRepository equipmentSetRepository;

    @Test
    void getById__WITHOUT_EQ__OK() throws Exception {
        // given
        String number = "EQ_SET_NUMBER_TEST_1";
        String name = "EQ_SET_NAME_TEST_1";
        String description = "EQ_SET_DESCRIPTION_TEST_1";
        var execResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(number, name, description)
        );
        Integer id = Integer.valueOf(
            execResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );

        // when
        var result = equipmentSetRepository.get(id);

        // then
        assertTrue(result.isPresent() && result.get().getId().equals(id));
    }

    @Test
    void getById__WITH_TWO_EQ__OK() throws Exception {
        // given
        // -- add EquipmentSet
        String number = "EQ_SET_NUMBER_TEST_1";
        String name = "EQ_SET_NAME_TEST_1";
        String description = "EQ_SET_DESCRIPTION_TEST_1";
        var execEqSetResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(number, name, description)
        );
        Integer eqSetId = Integer.valueOf(
            execEqSetResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment#1
        String eqNumber1 = "EQ_NUMBER_TEST_1";
        String eqName1 = "EQ_NAME_TEST_1";
        var execEqResult1 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(eqNumber1, eqName1, 1)
        );
        Integer eqId1 = Integer.valueOf(
            execEqResult1.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment#2
        String eqNumber2 = "EQ_NUMBER_TEST_2";
        String eqName2 = "EQ_NAME_TEST_2";
        var execEqResult2 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(eqNumber2, eqName2, 1)
        );
        Integer eqId2 = Integer.valueOf(
            execEqResult2.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add relation equipment#1 and set_equipment#1
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId1, eqSetId)
        );
        // -- add relation equipment#2 and set_equipment#1
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId2, eqSetId)
        );

        // when
        var result = equipmentSetRepository.get(eqSetId);

        // then
        assertTrue(result.isPresent()
            && result.get().getId().equals(eqSetId)
            && result.get().getEquipments().size() == 2
            && result.get().getEquipments()
            .stream()
            .map(SingleEquipmentSetRes.Equipment::getId)
            .toList()
            .containsAll(List.of(eqId1, eqId2))
        );
    }

    @Test
    void getById__NOT_FOUND() {
        // when
        var result = equipmentSetRepository.get(1);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void getByNumber__WITHOUT_EQ__OK() throws Exception {
        // given
        String number = "EQ_SET_NUMBER_TEST_1";
        String name = "EQ__SET_NAME_TEST_1";
        String description = "EQ_SET_NAME_TEST_1";
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(number, name, description)
        );

        // when
        var result = equipmentSetRepository.get(number);

        // then
        assertTrue(result.isPresent() && result.get().getNumber().equals(number));
    }

    @Test
    void getByNumber__WITH_TWO_EQ__OK() throws Exception {
        // given
        // -- add EquipmentSet
        String number = "EQ_SET_NUMBER_TEST_1";
        String name = "EQ_SET_NAME_TEST_1";
        String description = "EQ_SET_DESCRIPTION_TEST_1";
        var execEqSetResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(number, name, description)
        );
        Integer eqSetId = Integer.valueOf(
            execEqSetResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment#1
        String eqNumber1 = "EQ_NUMBER_TEST_1";
        String eqName1 = "EQ_NAME_TEST_1";
        var execEqResult1 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(eqNumber1, eqName1, 1)
        );
        Integer eqId1 = Integer.valueOf(
            execEqResult1.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add Equipment#2
        String eqNumber2 = "EQ_NUMBER_TEST_2";
        String eqName2 = "EQ_NAME_TEST_2";
        var execEqResult2 = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_INSERT_NEW.formatted(eqNumber2, eqName2, 1)
        );
        Integer eqId2 = Integer.valueOf(
            execEqResult2.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- add relation equipment#1 and set_equipment#1
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId1, eqSetId)
        );
        // -- add relation equipment#2 and set_equipment#1
        TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_COMP_INSERT_NEW.formatted(eqId2, eqSetId)
        );

        // when
        var result = equipmentSetRepository.get(number);

        // then
        assertTrue(result.isPresent()
            && result.get().getId().equals(eqSetId)
            && result.get().getEquipments().size() == 2
            && result.get().getEquipments()
            .stream()
            .map(SingleEquipmentSetRes.Equipment::getId)
            .toList()
            .containsAll(List.of(eqId1, eqId2))
        );
    }


    @Test
    void getByNumber__NOT_FOUND() {
        // when
        var result = equipmentSetRepository.get("EQ_SET_NUMBER_TEST_1");

        // then
        assertTrue(result.isEmpty());
    }


    @Test
    void create__OK() throws Exception {
        // given
        EquipmentSetCreateReq req = new EquipmentSetCreateReq();
        req.setName("EQ_SET_NAME");
        req.setNumber("EQ_SET_NUMBER");
        req.setDescription("EQ_SET_DESCRIPTION");
        var id = equipmentSetRepository.create(req);

        // when
        var execResult = TEST_DB_CONTAINER.execInContainer(
            "bash", "-c",
            SQL_EQ_SET_GET_ID_BY_NUMBER.formatted(req.getNumber())
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
        EquipmentSetCreateReq req = new EquipmentSetCreateReq();
        req.setName("EQ_SET_NAME");
        req.setNumber("EQ_SET_NUMBER");
        req.setDescription("EQ_SET_DESCRIPTION");
        equipmentSetRepository.create(req);

        // when
        Executable executable = () -> equipmentSetRepository.create(req);

        // then
        assertThrows(InsertEquipmentSetException.class, executable);
    }

    @Test
    void list__OK() throws Exception {
        // given
        EquipmentSetListReq filter = new EquipmentSetListReq();
        filter.setItemsPerPage(10);
        filter.setPage(2);
        filter.setSortCode(EquipmentSetSortCode.NAME_ASC);

        int total = 100;
        for (int i = 1; i <= total ; i++) {
            String nameIndex = (total + 1 - i) <= 9 ? "0" + (total + 1 - i) : String.valueOf(total + 1 - i);
            String name = "EQUIPMENT_SET_NAME__" + nameIndex;

            String numberIndex = i <= 9 ? "0" + i : String.valueOf(i);
            String number = "EQUIPMENT_SET_NUMBER__" + numberIndex;

            TEST_DB_CONTAINER.execInContainer("bash", "-c",
                SQL_EQ_SET_INSERT_NEW.formatted(number, name, "TEST_DESCRIPTION")
            );

        }

        // when
        var result = equipmentSetRepository.list(filter);

        // then
        // -- check total count
        assertEquals(total, result.getTotalCount());
        // -- check current count
        assertEquals(filter.getItemsPerPage(), result.getSets().size());
        // -- check offset && sort
        Integer indexStart = Integer.valueOf(result.getSets()
            .stream()
            .toList()
            .get(0).getName().split("__")[1]);
        assertEquals(filter.getItemsPerPage() * filter.getPage(), indexStart);
        Integer indexEnd = Integer.valueOf(result.getSets()
            .stream()
            .toList()
            .get(result.getSets().size() - 1).getName().split("__")[1]);
        assertEquals(filter.getItemsPerPage() * filter.getPage() + filter.getItemsPerPage() - 1, indexEnd);

    }

    @Test
    void addEquipments__OK() throws Exception {
        // given
        // -- insert EquipmentSet
        String number = "EQ_SET_NUMBER_TEST_1";
        String name = "EQ_SET_NAME_TEST_1";
        String description = "EQ_SET_DESCRIPTION_TEST_1";
        var execEqSetResult = TEST_DB_CONTAINER.execInContainer("bash", "-c",
            SQL_EQ_SET_INSERT_NEW.formatted(number, name, description)
        );
        Integer eqSetId = Integer.valueOf(
            execEqSetResult.getStdout()
                .split("\n")[2]
                .replace(" ", "")
        );
        // -- insert a few Equipments
        int equipmentsCount = 5;
        List<String> eqNumbers = new ArrayList<>();
        for (int i = 0; i < equipmentsCount; i++) {
            String eqNumber = "EQ_NUMBER_" + i;
            String eqName = "EQ_NAME_" + i;
            TEST_DB_CONTAINER.execInContainer("bash", "-c",
                SQL_EQ_INSERT_NEW.formatted(eqNumber, eqName, 1)
            );
            eqNumbers.add(eqNumber);
        }
        var equipmentSetAddEquipmentsReq = new EquipmentSetAddEquipmentsReq();
        equipmentSetAddEquipmentsReq.setNumbers(eqNumbers);
        // when
        var result = equipmentSetRepository.addEquipments(number, equipmentSetAddEquipmentsReq);

        // then
        assertEquals(equipmentsCount, result.getTotalInsertedCount());
        assertEquals(equipmentsCount, result.getExistedEquipmentIds().size());
    }

    // TODO: insert only not existed
    @Test
    void addEquipments__ONLY_NOT_EXISTED() throws Exception {
    }


}