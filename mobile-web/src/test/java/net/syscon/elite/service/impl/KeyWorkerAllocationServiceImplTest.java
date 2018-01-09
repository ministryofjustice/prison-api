package net.syscon.elite.service.impl;

import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
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
    private KeyWorkerAllocationService service;

    @Mock
    private KeyWorkerAllocationRepository repo;

    @Before
    public void setUp() throws Exception {
        service = new KeyWorkerAllocationServiceImpl(repo);
    }

    @Test
    public void shouldCallCollaboratorsForCreateAllocation () throws Exception {
        final KeyWorkerAllocation allocation = buildKeyWorkerAllocation();
        service.createAllocation(allocation, USER_1);
        verify(repo, times(1)).createAllocation(allocation, USER_1);
        verify(repo, times(1)).deactivateAllocationForOffenderBooking(allocation.getBookingId(), USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForGetAllocationHistoryForPrisoner () throws Exception {
        service.getAllocationHistoryForPrisoner(-1L);
        verify(repo, times(1)).getAllocationHistoryForPrisoner(-1L);
    }


    @Test
    public void shouldCallCollaboratorsForGetLatestAllocationForOffenderBooking () throws Exception {
        when(repo.getLatestAllocationForOffenderBooking(-1L)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        service.getLatestAllocationForOffenderBooking(-1L);
        verify(repo, times(1)).getLatestAllocationForOffenderBooking(-1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEmptyResultSetException_ForGetLatestAllocationForOffenderBooking () throws Exception {
        when(repo.getLatestAllocationForOffenderBooking(-1L)).thenReturn(Optional.empty());
        service.getLatestAllocationForOffenderBooking(-1L);
    }

    @Test
    public void shouldCallCollaboratorsForGetCurrentAllocationForOffenderBooking () throws Exception {
        when(repo.getCurrentAllocationForOffenderBooking(-1L)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        service.getCurrentAllocationForOffenderBooking(-1L);
        verify(repo, times(1)).getCurrentAllocationForOffenderBooking(-1L);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEmptyResultSetException_ForGetCurrentAllocationForOffenderBooking () throws Exception {
        when(repo.getCurrentAllocationForOffenderBooking(-1L)).thenReturn(Optional.empty());
        service.getCurrentAllocationForOffenderBooking(-1L);
    }

    @Test
    public void shouldCallCollaboratorsForDeactivateAllocationForKeyWorker () throws Exception {
        service.deactivateAllocationForKeyWorker(-1L, USER_1);
        verify(repo, times(1)).deactivateAllocationsForKeyWorker(-1L, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForDeactivateAllocationForOffenderBooking () throws Exception {
        service.deactivateAllocationForOffenderBooking(-1L, USER_1);
        verify(repo, times(1)).deactivateAllocationForOffenderBooking(-1L, USER_1);
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation() {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(-1L).reason("reason").staffId(-1L).type("A").build();
    }

}