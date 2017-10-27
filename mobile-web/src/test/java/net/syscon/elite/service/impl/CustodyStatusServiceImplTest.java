package net.syscon.elite.service.impl;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.support.CustodyStatus;
import net.syscon.elite.repository.CustodyStatusRecord;
import net.syscon.elite.repository.CustodyStatusRepository;
import net.syscon.elite.service.CustodyStatusCalculatorTest;
import net.syscon.elite.service.CustodyStatusService;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
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

    @Test
    @UseDataProvider("custodyStatusRecords")
    public void asAService(String booking_status, String active_flag, String direction_code, String movement_type, String movement_reason_code, CustodyStatus expectedCustodyStatus) {
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
        assertEquals("identifies correct custody status", custodyStatus.getCustodyStatus(), expectedCustodyStatus);
    }

}