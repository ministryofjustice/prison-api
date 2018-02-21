package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.impl.keyworker.KeyWorkerAllocationServiceImpl;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.util.Optional;

import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.verifyException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeyWorkerAllocationServiceImplTest {

    private static final String USER_1 = "USER1";
    private static final String AUTO_ALLOCATED_TYPE = "A";
    private static final String AUTO_ALLOCATION_TYPE = AUTO_ALLOCATED_TYPE;
    private static final String DEALLOCATION_REASON = "deallocating";
    private static final long BOOKING_ID = -1L;
    private static final String AGENCY_ID = "LEI";
    private static final long STAFF_ID = -2L;

    private KeyWorkerAllocationService service;

    @Mock
    private KeyWorkerAllocationRepository repo;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private  BookingService bookingService;

    @Before
    public void setUp() {
        service = new KeyWorkerAllocationServiceImpl(repo, authenticationFacade, bookingService);
    }

    @Test
    public void shouldRejectInvalidDateRangeForGetAllocations() {
        Throwable thrown = catchThrowable(() -> service.getAllocations(
                AGENCY_ID,
                LocalDate.of(2017, 12, 11),
                LocalDate.of(2016, 12, 11),
                "A",
                0L,
                10L,
                "field",
                Order.ASC));

        verifyException(thrown, BadRequestException.class, "Invalid date range: toDate is before fromDate.");
    }

    @Test
    public void shouldRejectFutureToDateForGetAllocations() {
        Throwable thrown = catchThrowable(() -> service.getAllocations(
                AGENCY_ID,
                LocalDate.of(2017, 12, 11),
                LocalDate.of(2032, 12, 11),
                "M",
                0L,
                10L,
                "field",
                Order.ASC));

        verifyException(thrown, BadRequestException.class, "Invalid date range: toDate cannot be in the future.");
    }

    @Test
    public void shouldCallCollaboratorsForCreateAllocation () {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        final KeyWorkerAllocation allocation = buildKeyWorkerAllocation(AUTO_ALLOCATION_TYPE);
        service.createAllocation(allocation, USER_1);
        verify(repo, times(1)).getCurrentAllocationForOffenderBooking(allocation.getBookingId());
        verify(repo, times(1)).createAllocation(allocation, USER_1);
    }
    
    @Test(expected = AllocationException.class)
    public void shouldThrowExceptionWhenExistingAllocationForOffenderBooking () {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        final KeyWorkerAllocation allocation = buildKeyWorkerAllocation(AUTO_ALLOCATION_TYPE);
        service.createAllocation(allocation, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForGetAllocationHistoryForPrisoner () {
        service.getAllocationHistoryForPrisoner(-1L, null, null);
        verify(repo, times(1)).getAllocationHistoryForPrisoner(-1L, "assigned", Order.DESC);
    }


    @Test
    public void shouldCallCollaboratorsForGetLatestAllocationForOffenderBooking () {
        when(repo.getLatestAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        service.getLatestAllocationForOffenderBooking(BOOKING_ID);
        verify(repo, times(1)).getLatestAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEmptyResultSetExceptionForGetLatestAllocationForOffenderBooking () {
        when(repo.getLatestAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        service.getLatestAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test
    public void shouldCallCollaboratorsForGetCurrentAllocationForOffenderBooking () {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.of(KeyWorkerAllocation.builder().build()));
        service.getCurrentAllocationForOffenderBooking(BOOKING_ID);
        verify(repo, times(1)).getCurrentAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEmptyResultSetExceptionForGetCurrentAllocationForOffenderBooking () {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        service.getCurrentAllocationForOffenderBooking(BOOKING_ID);
    }

    @Test
    public void shouldCallCollaboratorsForDeactivateAllocationForKeyWorker () {
        service.deactivateAllocationForKeyWorker(-1L, DEALLOCATION_REASON, USER_1);
        verify(repo, times(1)).deactivateAllocationsForKeyWorker(-1L, DEALLOCATION_REASON, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsForDeactivateAllocationForOffenderBooking () {
        service.deactivateAllocationForOffenderBooking(BOOKING_ID, DEALLOCATION_REASON, USER_1);
        verify(repo, times(1)).deactivateAllocationForOffenderBooking(BOOKING_ID, DEALLOCATION_REASON, USER_1);
    }

    @Test
    public void shouldCallCollaboratorsWithDefaultsForGetUnallocatedOffenders() {
        service.getUnallocatedOffenders("LEI", null, null, null, null);
        verify(repo, times(1)).getUnallocatedOffenders(ImmutableSet.of("LEI"), 0L,10L, "lastName", Order.ASC);
    }

    @Test
    public void shouldCallCollaboratorsForGetUnallocatedOffenders() {
        service.getUnallocatedOffenders("LEI", 5L, 10L, "firstName", Order.DESC);
        verify(repo, times(1)).getUnallocatedOffenders(ImmutableSet.of("LEI"), 5L,10L, "firstName", Order.DESC);
    }

    @Test
    public void shouldCallCollaboratorsForGetAllocatedOffenders() {
        service.getAllocations("LEI", LocalDate.parse("2017-04-01"), LocalDate.parse("2017-07-01"), AUTO_ALLOCATED_TYPE,5L, 10L, "firstName", Order.DESC);
        verify(repo, times(1)).getAllocatedOffenders(ImmutableSet.of("LEI"), LocalDate.parse("2017-04-01"), LocalDate.parse("2017-07-01"), AUTO_ALLOCATED_TYPE,5L,10L, "firstName", Order.DESC);
    }

    @Test
    public void shouldCallCollaboratorsWhenAllOptionalParametersOmittedForGetAllocatedOffenders() {
        service.getAllocations("LEI", null, null, null, null, null, null, null);
        verify(repo, times(1)).getAllocatedOffenders(ImmutableSet.of("LEI"), null, null, null,0L,10L, "lastName,firstName", Order.ASC);
    }

    @Test
    public void shouldCallCollaboratorsForGetAvailableKeyworkers() {
        service.getAvailableKeyworkers(AGENCY_ID);
        verify(repo, times(1)).getAvailableKeyworkers(AGENCY_ID);
    }

    @Test
    public void testGetKeyworkerDetails() throws Exception {

        when(repo.getKeyworkerDetails(STAFF_ID))
                .thenReturn(Optional.of(Keyworker.builder().firstName("me").build()));

        final Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);
        assertThat(keyworker.getFirstName()).isEqualTo("me");
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetKeyworkerDetailsNotFound() throws Exception {
        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.empty());

        service.getKeyworkerDetails(STAFF_ID);
    }

    private KeyWorkerAllocation buildKeyWorkerAllocation(String type) {
        return KeyWorkerAllocation.builder().agencyId("LEI").bookingId(BOOKING_ID).reason("reason").staffId(-1L).type(type).build();
    }
}
