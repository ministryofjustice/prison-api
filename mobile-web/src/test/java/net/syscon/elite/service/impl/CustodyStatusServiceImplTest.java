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

import java.util.Arrays;
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
                        .build(),
                CustodyStatusRecord.builder()
                        .offender_id_display("B")
                        .booking_status("O")
                        .active_flag("Y")
                        .direction_code("OUT")
                        .movement_type("CRT")
                        .build(),
                CustodyStatusRecord.builder()
                        .offender_id_display("C")
                        .booking_status("O")
                        .active_flag("Y")
                        .build()
        );

        when(custodyStatusRepository.listCustodyStatusRecords())
                .thenReturn(records);
    }


    @Test
    @UseDataProvider("custodyStatusRecords")
    public void getCustodyStatusRecord(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatusCode expectedCustodyStatus) {
        String randomOffenderNo = UUID.randomUUID().toString();

        when(custodyStatusRepository.getCustodyStatusRecord(randomOffenderNo))
                .thenReturn(Optional.of(CustodyStatusRecord
                        .builder()
                        .offender_id_display(randomOffenderNo)
                        .booking_status(booking_status)
                        .active_flag(active_flag)
                        .direction_code(direction_code)
                        .movement_type(movement_type)
                        .movement_reason_code(movement_reason_code)
                        .build()));

        PrisonerCustodyStatus custodyStatus = service.getCustodyStatus(randomOffenderNo);

        assertEquals("has the correct offenderNo", custodyStatus.getOffenderNo(), randomOffenderNo);
        assertEquals("identifies correct custody status", custodyStatus.getCustodyStatusCode(), expectedCustodyStatus);
    }

    @Test
    public void listCustodyStatusRecordsWithoutACustodyStatusFilter() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(null, null);

        assertEquals(3, records.size());
    }

    @Test
    public void listCustodyStatusRecordsWithACustodyStatusFilterSetToACTIVE_IN() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(CustodyStatusCode.ACTIVE_IN);

        assertEquals(1, records.size());
        PrisonerCustodyStatus record = records.get(0);

        assertNotNull(record);
        assertEquals("C", record.getOffenderNo());
        assertEquals(CustodyStatusCode.ACTIVE_IN, record.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_IN.toString(), record.getCustodyStatusDescription());
    }

    @Test
    public void listCustodyStatusRecordsWithACustodyStatusFilterSetToACTIVE_OUT_CRT() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(CustodyStatusCode.ACTIVE_OUT_CRT);

        assertEquals(1, records.size());
        PrisonerCustodyStatus record = records.get(0);

        assertNotNull(record);
        assertEquals("B", record.getOffenderNo());
        assertEquals(CustodyStatusCode.ACTIVE_OUT_CRT, record.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_OUT_CRT.toString(), record.getCustodyStatusDescription());
    }

    @Test
    public void listCustodyStatusRecordsWithACustodyStatusFilterSetToTwoCodes() {
        List<CustodyStatusCode> codes = Arrays.asList(CustodyStatusCode.ACTIVE_OUT_CRT, CustodyStatusCode.ACTIVE_IN);
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(codes, Order.ASC);

        assertEquals(2, records.size());
        PrisonerCustodyStatus record = records.get(0);

        assertNotNull(record);
        assertEquals("C", record.getOffenderNo());
        assertEquals(CustodyStatusCode.ACTIVE_IN, record.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_IN.toString(), record.getCustodyStatusDescription());

        PrisonerCustodyStatus record2 = records.get(1);

        assertNotNull(record2);
        assertEquals("B", record2.getOffenderNo());
        assertEquals(CustodyStatusCode.ACTIVE_OUT_CRT, record2.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_OUT_CRT.toString(), record2.getCustodyStatusDescription());
    }

}