package net.syscon.elite.api.resource.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.resource.KeyWorkerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import net.syscon.elite.service.keyworker.KeyworkerAutoAllocationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeyWorkerResourceImplTest {

    private static final String AUTO_ALLOCATED_TYPE = "A";
    private static final String MANUAL_ALLOCATED_TYPE = "M";
    private static final long BOOKING_ID = -1L;
    private static final String AGENCY_ID = "LEI";

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Mock
    private KeyWorkerAllocationService allocationService;

    @Mock
    private KeyworkerAutoAllocationService autoAllocationService;

    private KeyWorkerResourceImpl resource;

    @Before
    public void setUp(){
        resource = new KeyWorkerResourceImpl(allocationService, autoAllocationService);
    }

    @Test
    public void shouldAllowValidDateRangeForGetAllocatios() {
        List<KeyWorkerAllocationDetail> allocations = ImmutableList.of(buildKeyWorkerAllocationDetails(AUTO_ALLOCATED_TYPE));

        when(allocationService.getAllocations(anyString(), anyObject(), anyObject(), anyString(), anyLong(), anyLong(),
                anyString(), anyObject())).thenReturn(new Page<>(allocations,1, 0, 10));

        KeyWorkerResource.GetAllocationsResponse response = resource.getAllocations(AGENCY_ID, MANUAL_ALLOCATED_TYPE,"2016-12-11","2016-12-11", 0L,
                10L, "field", Order.ASC);

        verify(allocationService, times(1)).getAllocations(AGENCY_ID, LocalDate.parse("2016-12-11"),
                LocalDate.parse("2016-12-11"), MANUAL_ALLOCATED_TYPE, 0L,
                10L, "field", Order.ASC);

        assertThat(response.getEntity()).isEqualTo(allocations);
    }

    private KeyWorkerAllocationDetail buildKeyWorkerAllocationDetails(String type) {
        return KeyWorkerAllocationDetail.builder().agencyId(AGENCY_ID).bookingId(BOOKING_ID).staffId(-1L).allocationType(type).build();
    }
}