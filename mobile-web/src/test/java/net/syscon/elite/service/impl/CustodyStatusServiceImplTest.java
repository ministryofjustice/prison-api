package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.service.support.CustodyStatusDto;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link CustodyStatusServiceImpl}.
 */
@RunWith(DataProviderRunner.class)
public class CustodyStatusServiceImplTest {

    private CustodyStatusRepository custodyStatusRepository = mock(CustodyStatusRepository.class);

    private CustodyStatusService service = new CustodyStatusServiceImpl(custodyStatusRepository);

    @DataProvider
    public static Object[][] custodyStatusRecords() {
        return CustodyStatusCalculatorTest.custodyStatusRecords();
    }

    @Before
    public void init() {
        List<CustodyStatusDto> records = ImmutableList.of(
                CustodyStatusDto.builder()
                        .offenderIdDisplay("A")
                        .build(),
                CustodyStatusDto.builder()
                        .offenderIdDisplay("B")
                        .bookingStatus("O")
                        .activeFlag("Y")
                        .directionCode("OUT")
                        .movementType("CRT")
                        .build(),
                CustodyStatusDto.builder()
                        .offenderIdDisplay("C")
                        .bookingStatus("O")
                        .activeFlag("Y")
                        .build()
        );

        when(custodyStatusRepository.listCustodyStatuses())
                .thenReturn(records);
    }


    @Test
    @UseDataProvider("custodyStatusRecords")
    public void getCustodyStatus(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatusCode expectedCustodyStatus) {
        String randomOffenderNo = UUID.randomUUID().toString();

        when(custodyStatusRepository.getCustodyStatus(randomOffenderNo))
                .thenReturn(Optional.of(CustodyStatusDto
                        .builder()
                        .offenderIdDisplay(randomOffenderNo)
                        .bookingStatus(booking_status)
                        .activeFlag(active_flag)
                        .directionCode(direction_code)
                        .movementType(movement_type)
                        .movementReasonCode(movement_reason_code)
                        .build()));

        PrisonerCustodyStatus custodyStatus = service.getCustodyStatus(randomOffenderNo);

        assertEquals("has the correct offenderNo", custodyStatus.getOffenderNo(), randomOffenderNo);
        assertEquals("identifies correct custody status", custodyStatus.getCustodyStatusCode(), expectedCustodyStatus);
    }

    @Test
    public void listCustodyStatusesWithoutACustodyStatusFilter() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(new CustodyStatusCode[] {}, null);

        assertEquals(3, records.size());
    }

    @Test
    public void listCustodyStatusesWithACustodyStatusFilterSetToACTIVEIN() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(CustodyStatusCode.ACTIVE_IN);

        assertEquals(1, records.size());
        PrisonerCustodyStatus record = records.get(0);

        assertNotNull(record);
        assertEquals("C", record.getOffenderNo());
        assertEquals(CustodyStatusCode.ACTIVE_IN, record.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_IN.toString(), record.getCustodyStatusDescription());
    }

    @Test
    public void listCustodyStatusesWithACustodyStatusFilterSetToACTIVEOUTCRT() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(CustodyStatusCode.ACTIVE_OUT_CRT);

        assertEquals(1, records.size());
        PrisonerCustodyStatus record = records.get(0);

        assertNotNull(record);
        assertEquals("B", record.getOffenderNo());
        assertEquals(CustodyStatusCode.ACTIVE_OUT_CRT, record.getCustodyStatusCode());
        assertEquals(CustodyStatusCode.ACTIVE_OUT_CRT.toString(), record.getCustodyStatusDescription());
    }

    @Test
    public void listCustodyStatusesWithACustodyStatusFilterSetToTwoCodes() {
        CustodyStatusCode[] codes = new CustodyStatusCode[] { CustodyStatusCode.ACTIVE_OUT_CRT, CustodyStatusCode.ACTIVE_IN };
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