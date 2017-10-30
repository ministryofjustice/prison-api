package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.service.CustodyStatusCalculatorTest;
import net.syscon.elite.service.CustodyStatusService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link CustodyStatusServiceImpl}.
 */
@RunWith(DataProviderRunner.class)
public class CustodyStatusServiceImplTest {

    CustodyStatusRepository custodyStatusRepository = mock(CustodyStatusRepository.class);

    CustodyStatusService service = new CustodyStatusServiceImpl(custodyStatusRepository);

    @DataProvider
    public static Object[][] custodyStatusRecords() {
        return CustodyStatusCalculatorTest.custodyStatusRecords();
    }

    @Before
    public void init() {
        List<CustodyStatusRecord> records = ImmutableList.of(
                CustodyStatusRecord.builder()
                        .offender_id_display("A")
                        .agy_loc_id("LEI")
                        .build(),
                CustodyStatusRecord.builder()
                        .offender_id_display("B")
                        .agy_loc_id("BMI")
                        .build(),
                CustodyStatusRecord.builder()
                        .offender_id_display("C")
                        .agy_loc_id("MAI")
                        .booking_status("O")
                        .active_flag("Y")
                        .build()
        );

        when(custodyStatusRepository.listCustodyStatusRecords(null, null, null))
                .thenReturn(records);

        when(custodyStatusRepository.listCustodyStatusRecords("LEI", null, null))
                .thenReturn(records
                                .stream()
                                .filter(x -> "LEI".equals(x.getAgy_loc_id()))
                                .collect(Collectors.toList()));

        when(custodyStatusRepository.listCustodyStatusRecords("BMI", null, null))
                .thenReturn(records
                                .stream()
                                .filter(x -> "BMI".equals(x.getAgy_loc_id()))
                                .collect(Collectors.toList()));
    }


    @Test
    @UseDataProvider("custodyStatusRecords")
    public void getCustodyStatusRecord(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatusCode expectedCustodyStatus) {
        String randomOffenderNo = UUID.randomUUID().toString();
        String randomLocationId = UUID.randomUUID().toString();

        when(custodyStatusRepository.getCustodyStatusRecord(randomOffenderNo))
                .thenReturn(Optional.of(CustodyStatusRecord
                        .builder()
                        .offender_id_display(randomOffenderNo)
                        .agy_loc_id(randomLocationId)
                        .booking_status(booking_status)
                        .active_flag(active_flag)
                        .direction_code(direction_code)
                        .movement_type(movement_type)
                        .movement_reason_code(movement_reason_code)
                        .build()));

        PrisonerCustodyStatus custodyStatus = service.getCustodyStatus(randomOffenderNo);

        assertEquals("has the correct offenderNo", custodyStatus.getOffenderNo(), randomOffenderNo);
        assertEquals("has the correct locationId", custodyStatus.getLocationId(), randomLocationId);
        assertEquals("identifies correct custody status", custodyStatus.getCustodyStatusCode(), expectedCustodyStatus);
    }

    @Test
    public void listCustodyStatusRecordsWithoutLocationFilter() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(null, null,null, null);

        assertEquals(3, records.size());
    }

    @Test
    public void listCustodyStatusRecordsWithALocationFilterSetToLEI() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses("LEI", null,null, null);

        assertEquals(1, records.size());
        assertEquals("A", records.get(0).getOffenderNo());
        assertEquals("LEI", records.get(0).getLocationId());
    }

    @Test
    public void listCustodyStatusRecordsWithALocationFilterSetToBMI() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses("BMI", null,null, null);

        assertEquals(1, records.size());
        assertEquals("B", records.get(0).getOffenderNo());
        assertEquals("BMI", records.get(0).getLocationId());
    }

    @Test
    public void listCustodyStatusRecordsWithACustodyStatusFilterSetToACTIVE_IN() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(null, CustodyStatusCode.ACTIVE_IN,null, null);

        assertEquals(1, records.size());
        PrisonerCustodyStatus record = records.get(0);

        assertNotNull(record);
        assertEquals("C", record.getOffenderNo());
        assertEquals("MAI", record.getLocationId());
        assertEquals(CustodyStatusCode.ACTIVE_IN, record.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_IN.toString(), record.getCustodyStatusDescription());
    }

    @Test
    public void listCustodyStatusRecordsWithACustodyStatusFilterSetToIN_ACTIVE() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(null, CustodyStatusCode.IN_ACTIVE,null, null);

        assertEquals(0, records.size());
    }

}