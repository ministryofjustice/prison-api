package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.service.CustodyStatusCalculatorTest;
import net.syscon.elite.service.CustodyStatusService;
import net.syscon.elite.service.support.CustodyStatusDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
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

    private LocalDate nowDate = LocalDate.now();

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

        when(custodyStatusRepository.listCustodyStatuses(nowDate))
                .thenReturn(records);
    }

    @Test
    @UseDataProvider("custodyStatusRecords")
    public void canRetrieveCustodyStatusRecordsWithCorrectCustodyStatusCodesAttached(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatusCode expectedCustodyStatus) {
        String randomOffenderNo = UUID.randomUUID().toString();

        when(custodyStatusRepository.getCustodyStatus(randomOffenderNo, nowDate))
                .thenReturn(Optional.of(CustodyStatusDto
                        .builder()
                        .offenderIdDisplay(randomOffenderNo)
                        .bookingStatus(booking_status)
                        .activeFlag(active_flag)
                        .directionCode(direction_code)
                        .movementType(movement_type)
                        .movementReasonCode(movement_reason_code)
                        .build()));

        PrisonerCustodyStatus custodyStatus = service.getCustodyStatus(randomOffenderNo, nowDate);

        assertEquals("has the correct offenderNo", custodyStatus.getOffenderNo(), randomOffenderNo);
        assertEquals("identifies correct custody status", custodyStatus.getCustodyStatusCode(), expectedCustodyStatus);
    }

    @Test
    public void canRetrieveAllCustodyStatusesWhenTheFilterListIsEmpty() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(Lists.newArrayList(), nowDate, null);

        assertEquals(3, records.size());
    }

    @Test
    public void canRetrieveAllActiveINCustodyStatusesWhenTheFilterListIsSetToACTIVEIN() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(Lists.newArrayList(CustodyStatusCode.ACTIVE_IN), nowDate, null);

        assertEquals(1, records.size());
        assertEquals("C", records.get(0).getOffenderNo());
    }

    @Test
    public void canRetrieveAllACTIVEOUTCRTCustodyStatusesWhenTheFilterListIsSetToACTIVEOUTCRT() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(Lists.newArrayList(CustodyStatusCode.ACTIVE_OUT_CRT), nowDate, null);

        assertEquals(1, records.size());
        assertEquals("B", records.get(0).getOffenderNo());
    }

    @Test
    public void canRetrieveAllACTIVEOUTCRTandACTIVEINCustodyStatusesWhenTheFilterListIsSetToACTIVEOUTCRTandACTIVEIN() {
        List<CustodyStatusCode> codes = Lists.newArrayList(CustodyStatusCode.ACTIVE_OUT_CRT, CustodyStatusCode.ACTIVE_IN);
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(codes, nowDate, Order.ASC);

        assertEquals(2, records.size());
        assertEquals("C", records.get(0).getOffenderNo());
        assertEquals("B", records.get(1).getOffenderNo());
    }

    @Test
    public void canRetrieveCustodyStatusesInDescendingOrder() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(Lists.newArrayList(), nowDate, Order.DESC);

        assertEquals("A", records.get(0).getOffenderNo());
        assertEquals("C", records.get(2).getOffenderNo());
    }

    @Test
    public void canRetrieveCustodyStatusesInAscendingOrder() {
        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(Lists.newArrayList(), nowDate, Order.ASC);

        assertEquals("C", records.get(0).getOffenderNo());
        assertEquals("A", records.get(2).getOffenderNo());
    }

    @Test
    public void canPassLocalDateToTheRepositoryToTailorListOfResultsToASpecificPointInTime() {
        final List emptyList = Lists.newArrayList();
        final LocalDate localDate = LocalDate.of(2016,5,7);

        when(custodyStatusRepository.listCustodyStatuses(localDate))
                .thenReturn(ImmutableList.of(CustodyStatusDto.builder().build()));

        List<PrisonerCustodyStatus> records = service.listCustodyStatuses(emptyList, localDate, null);

        assertEquals("A list of results is returned", 1, records.size());
    }

    @Test
    public void canPassLocalDateToTheRepositoryToTailorResultToASpecificPointInTime() {
        final String offenderNo = "X";
        final LocalDate localDate = LocalDate.of(2016,5,7);

        when(custodyStatusRepository.getCustodyStatus(offenderNo, localDate))
                .thenReturn(Optional.of(CustodyStatusDto.builder().build()));

        assertNotNull("A Custody Status is returned", service.getCustodyStatus(offenderNo, localDate));
    }

}