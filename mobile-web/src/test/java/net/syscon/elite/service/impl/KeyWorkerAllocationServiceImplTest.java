package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeyWorkerAllocationServiceImplTest {

    private static final String USER_1 = "USER1";
    private static final String AUTO_ALLOCATION_TYPE = "A";
    private static final String DEALLOCATION_REASON = "deallocating";
    private static final long BOOKING_ID = -1L;
    private static final String AGENCY_ID = "LEI";
    private KeyWorkerAllocationService service;

    @Mock
    private KeyWorkerAllocationRepository repo;

    @Before
    public void setUp() throws Exception {
        service = new KeyWorkerAllocationServiceImpl(repo);
    }

    @Test
    public void shouldCallCollaboratorsForCreateAllocation () throws Exception {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        final KeyWorkerAllocation allocation = buildKeyWorkerAllocation(AUTO_ALLOCATION_TYPE);
        service.createAllocation(allocation, USER_1);
        verify(repo, times(1)).getCurrentAllocationForOffenderBooking(allocation.getBookingId());
        verify(repo, times(1)).createAllocation(allocation, USER_1);
    }

    @Test(expected = AllocationException.class)
    public void shouldThrowException_existingAllocationForOffenderBooking () throws Exception {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        final KeyWorkerAllocation allocation = buildKeyWorkerAllocation(AUTO_ALLOCATION_TYPE);
        service.createAllocation(allocation, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForGetAllocationHistoryForPrisoner () throws Exception {
        service.getAllocationHistoryForPrisoner(-1L, null, null);
        verify(repo, times(1)).getAllocationHistoryForPrisoner(-1L, "assigned", Order.DESC);
    }


    @Test
    public void shouldCallCollaboratorsForGetLatestAllocationForOffenderBooking () throws Exception {
        when(repo.getLatestAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        service.getLatestAllocationForOffenderBooking(BOOKING_ID);
        verify(repo, times(1)).getLatestAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEmptyResultSetException_ForGetLatestAllocationForOffenderBooking () throws Exception {
        when(repo.getLatestAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        service.getLatestAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test
    public void shouldCallCollaboratorsForGetCurrentAllocationForOffenderBooking () throws Exception {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        service.getCurrentAllocationForOffenderBooking(BOOKING_ID);
        verify(repo, times(1)).getCurrentAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEmptyResultSetException_ForGetCurrentAllocationForOffenderBooking () throws Exception {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        service.getCurrentAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test
    public void shouldCallCollaboratorsForDeactivateAllocationForKeyWorker () throws Exception {
        service.deactivateAllocationForKeyWorker(-1L, DEALLOCATION_REASON, USER_1);
        verify(repo, times(1)).deactivateAllocationsForKeyWorker(-1L, DEALLOCATION_REASON, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForDeactivateAllocationForOffenderBooking () throws Exception {
        service.deactivateAllocationForOffenderBooking(BOOKING_ID, DEALLOCATION_REASON, USER_1);
        verify(repo, times(1)).deactivateAllocationForOffenderBooking(BOOKING_ID, DEALLOCATION_REASON, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsWithDefaultsForGetUnallocatedOffenders() throws Exception {
        final Page<OffenderSummary> offenderSummaryPage = new Page<>(ImmutableList.of(buildOffenderSummary()), 1, 0, 100);
        when(repo.getUnallocatedOffenders(ImmutableSet.of("LEI"), 0L, 10L, "lastName", Order.ASC)).thenReturn(offenderSummaryPage);
        service.getUnallocatedOffenders(ImmutableSet.of("LEI"), null, null, null, null);
        verify(repo, times(1)).getUnallocatedOffenders(ImmutableSet.of("LEI"), 0L,10L, "lastName", Order.ASC);
    }

    @Test
    public void shouldCallCollaboratorsForGetUnallocatedOffenders() throws Exception {
        final Page<OffenderSummary> offenderSummaryPage = new Page<>(ImmutableList.of(buildOffenderSummary()), 1, 5, 10);
        when(repo.getUnallocatedOffenders(ImmutableSet.of("LEI"), 5L, 10L, "firstName", Order.DESC)).thenReturn(offenderSummaryPage);
        service.getUnallocatedOffenders(ImmutableSet.of("LEI"), 5L, 10L, "firstName", Order.DESC);
        verify(repo, times(1)).getUnallocatedOffenders(ImmutableSet.of("LEI"), 5L,10L, "firstName", Order.DESC);
    }

    @Test
    public void shouldCallCollaboratorsForGetAvailableKeyworkers() throws Exception {
        service.getAvailableKeyworkers(AGENCY_ID);
        verify(repo, times(1)).getAvailableKeyworkers(AGENCY_ID);
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation(String type) {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(BOOKING_ID).reason("reason").staffId(-1L).type(type).build();
    }

    private OffenderSummary buildOffenderSummary() {
        return OffenderSummary.builder().bookingId(-1L).build();
    }

}