package net.syscon.elite.service.impl;

import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.KeyWorkerAllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        verify(repo, times(1)).deactivateCurrentAllocationForOffenderBooking(allocation.getBookingId(), USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForGetAllocationAllocationHistoryForPrisoner () throws Exception {
        service.getAllocationHistoryForPrisoner(-1L);
        verify(repo, times(1)).getAllocationHistoryForPrisoner(-1L);
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation() {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(-1L).reason("reason").staffId(-1L).type("A").build();
    }

}