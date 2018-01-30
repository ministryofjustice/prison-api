package net.syscon.elite.api.resource.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeyWorkerResourceImplTest {

    private static final String AUTO_ALLOCATED_TYPE = "A";
    private static final String MANUAL_ALLOCATED_TYPE = "M";
    private static final long BOOKING_ID = -1L;
    private static final String AGENCY_ID = "LEI";
    private static final String AGENCY_ID_2 = "LXX";

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Mock
    private KeyWorkerAllocationService allocationService;

    @Mock
    private AgencyService agencyService;

    private KeyWorkerResourceImpl resource;

    @Before
    public void setUp() throws Exception {
        resource = new KeyWorkerResourceImpl(agencyService, allocationService);
    }

    @Test
    public void shouldAllowValidDateRangeForGetAllocatedOffenders() throws Exception {
        when(agencyService.getAgencyIds()).thenReturn(ImmutableSet.of(AGENCY_ID, AGENCY_ID_2));
        when(allocationService.getAllocatedOffenders(anySetOf(String.class), anyObject(), anyObject(), anyString(), anyLong(), anyLong(),
                anyString(), anyObject())).thenReturn(new Page<>(ImmutableList.of(buildKeyWorkerAllocationDetails(AUTO_ALLOCATED_TYPE)),1, 0, 10));
        resource.getAllocatedOffenders(null, MANUAL_ALLOCATED_TYPE,"2016-12-11","2016-12-11", 0L,
                10L, "field", Order.ASC);
        verify(allocationService, times(1)).getAllocatedOffenders(ImmutableSet.of(AGENCY_ID, AGENCY_ID_2), LocalDate.parse("2016-12-11"),
                LocalDate.parse("2016-12-11"), MANUAL_ALLOCATED_TYPE, 0L,
                10L, "field", Order.ASC);
    }

    @Test
    public void shouldRejectInvalidDateRangeForGetAllocatedOffenders() throws Exception {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Invalid date range: toDate is before fromDate");

        when(agencyService.getAgencyIds()).thenReturn(ImmutableSet.of(AGENCY_ID));
        resource.getAllocatedOffenders(AGENCY_ID, MANUAL_ALLOCATED_TYPE,"2017-12-11","2016-12-11", 0L,
                10L, "field", Order.ASC);
    }

    @Test
    public void shouldRejectFutureToDateForGetAllocatedOffenders() throws Exception {
        thrown.expect(BadRequestException.class);
        thrown.expectMessage("Invalid date range: toDate cannot be in the future.");

        when(agencyService.getAgencyIds()).thenReturn(ImmutableSet.of(AGENCY_ID));
        resource.getAllocatedOffenders(AGENCY_ID, MANUAL_ALLOCATED_TYPE,"2017-12-11","2032-12-11", 0L,
                10L, "field", Order.ASC);
    }

    @Test
    public void shouldRejectUnauthorisedAgencyAccessForGetAllocatedOffenders() throws Exception {
        thrown.expect(EntityNotFoundException.class);
        thrown.expectMessage("Agency with id LEI not found.");

        when(agencyService.getAgencyIds()).thenReturn(ImmutableSet.of("NON"));
        resource.getAllocatedOffenders(AGENCY_ID, MANUAL_ALLOCATED_TYPE,"2016-12-11","2016-12-11", 0L,
                10L, "field", Order.ASC);
    }
    
    private KeyWorkerAllocationDetail buildKeyWorkerAllocationDetails(String type) {
        return KeyWorkerAllocationDetail.builder().agencyId(AGENCY_ID).bookingId(BOOKING_ID).staffId(-1L).allocationType(type).build();
    }
}