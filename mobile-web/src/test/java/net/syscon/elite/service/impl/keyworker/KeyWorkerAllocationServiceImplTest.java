package net.syscon.elite.service.impl.keyworker;

import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.BookingService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.BadRequestException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KeyWorkerAllocationServiceImplTest {

    private static final String USER_1 = "USER1";
    private static final String AUTO_ALLOCATED_TYPE = "A";
    private static final String DEALLOCATION_REASON = "deallocating";
    private static final long BOOKING_ID = -1L;
    private static final String AGENCY_ID = "LEI";
    private static final long STAFF_ID = -2L;
    private static final long ANOTHER_STAFF_ID = -7L;

    private KeyWorkerAllocationService service;

    @Mock
    private KeyWorkerAllocationRepository repo;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private BookingService bookingService;

    @Before
    public void setUp() {
        service = new KeyWorkerAllocationServiceImpl(repo, authenticationFacade, bookingService);

        ReflectionTestUtils.setField(service, "deallocationBufferHours", 48);
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
        KeyWorkerAllocation allocation = getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID);
        service.createAllocation(allocation, USER_1);
        verify(repo, times(1)).getCurrentAllocationForOffenderBooking(allocation.getBookingId());
        verify(repo, times(1)).createAllocation(allocation, USER_1);
    }
    
    @Test(expected = AllocationException.class)
    public void shouldThrowExceptionWhenExistingAllocationForOffenderBooking () {
        when(repo.getCurrentAllocationForOffenderBooking(BOOKING_ID)).thenReturn(Optional.empty());
        KeyWorkerAllocation allocation = getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID);
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

    // Given a KW with staffId of N does exist
    // When KW details requested for KW with staffId of N
    // Then KW details are returned for KW with staffId of N
    @Test
    public void testGetKeyworkerDetails() {

        when(repo.getKeyworkerDetails(STAFF_ID))
                .thenReturn(Optional.of(Keyworker.builder().firstName("me").build()));

        final Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);
        assertThat(keyworker.getFirstName()).isEqualTo("me");
    }

    // Given a KW with staffId of N does not exist
    // When KW details requested for KW with staffId of N
    // Then an EntityNotFoundException is thrown
    @Test(expected = EntityNotFoundException.class)
    public void testGetKeyworkerDetailsNotFound() {
        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.empty());

        service.getKeyworkerDetails(STAFF_ID);
    }

    // Given offender X is released
    // And their allocation to Key worker A is expired on release
    // And it is less than 48 hours since release of offender X
    // And offender X has not been re-admitted to any agency
    // When details are requested for Key worker A
    // Then number of allocations for Key worker A includes all active allocations and expired allocation to offender X
    //
    // If this test fails, a Key worker may appear to have more capacity than they should and may be allocated
    // offenders when they normally would not have been.
    @Test
    public void testGetKeyworkerDetailsWhenAllocationRecentlyExpiredDueToOffenderRelease() {
        // A set of allocations for KW including an allocation for an offender that expired only 2 hours ago
        KeyWorkerAllocation expiredAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusDays(7));

        expiredAllocation = expireAllocation(expiredAllocation, null, LocalDateTime.now().minusHours(2));

        List<KeyWorkerAllocation> kwAllocations = Arrays.asList(
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 5, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 6, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 7, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 8, STAFF_ID),
                expiredAllocation
        );

        // Offender summary for offender who is subject of expired allocation
        OffenderSummary summary = getOffender(BOOKING_ID, AGENCY_ID, "A4565DF", false);

        // Expectations
        //  - repo to return a known Key worker with known number of active allocations
        //  - repo to return known set of KW allocations, including a recently expired allocation
        //  - booking service to return inactive booking summary for same booking in same agency
        int expectedAllocationCount = 4;

        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.of(getKeyworker(STAFF_ID, expectedAllocationCount)));
        when(repo.getAllocationsForKeyworker(STAFF_ID)).thenReturn(kwAllocations);
        when(bookingService.getLatestBookingByBookingId(BOOKING_ID)).thenReturn(summary);

        // Call service method
        Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);

        // Assertions - number of allocations should include active allocations plus the recently expired allocation
        //              because offender who is subject of recently expired allocation does not have an active booking
        //              in any agency (either same or different)
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount + 1);

        // Verifications
        verify(repo, atLeastOnce()).getKeyworkerDetails(eq(STAFF_ID));
        verify(repo, atLeastOnce()).getAllocationsForKeyworker(eq(STAFF_ID));
        verify(bookingService, atLeastOnce()).getLatestBookingByBookingId(eq(BOOKING_ID));
    }

    // Given offender X is released
    // And their allocation to Key worker A is expired
    // And it is 48 hours or more since release of offender X
    // When details are requested for Key worker A
    // Then number of allocations for Key worker A includes active allocations only
    //
    // If this test fails, a Key worker may appear to have less capacity than they actually do and may not be allocated
    // offenders when they normally would have been.
    @Test
    public void testGetKeyworkerDetailsWhenAllocationExpiredDueToOffenderRelease() {
        // A set of allocations for KW including an allocation for an offender that expired 3 days ago
        KeyWorkerAllocation expiredAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusDays(8));

        expiredAllocation = expireAllocation(expiredAllocation, null, LocalDateTime.now().minusDays(3));

        List<KeyWorkerAllocation> kwAllocations = Arrays.asList(
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 5, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 6, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 7, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 8, STAFF_ID),
                expiredAllocation
        );

        // Expectations
        //  - repo to return a known Key worker with known number of active allocations
        //  - repo to return known set of KW allocations, including a recently expired allocation
        int expectedAllocationCount = 8;

        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.of(getKeyworker(STAFF_ID, expectedAllocationCount)));
        when(repo.getAllocationsForKeyworker(STAFF_ID)).thenReturn(kwAllocations);

        // Call service method
        Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);

        // Assertions - number of allocations should include active allocations only, not the expired allocation as it
        //              expired before buffer period cut-off
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount);

        // Verifications
        verify(repo, atLeastOnce()).getKeyworkerDetails(eq(STAFF_ID));
        verify(repo, atLeastOnce()).getAllocationsForKeyworker(eq(STAFF_ID));
        verify(bookingService, never()).getLatestBookingByBookingId(anyLong());
    }

    // Given offender X is released
    // And their allocation to Key worker A is expired
    // And it is less than 48 hours since release of offender X
    // And offender X has been re-allocated to same Key worker in same agency
    // When details are requested for Key worker A
    // Then number of allocations for Key worker A includes active allocations only
    //
    // If this test fails, the allocation of an offender to a Key worker may be counted twice.
    @Test
    public void testGetKeyworkerDetailsWhenAllocationRecentlyExpiredAndSameOffenderReallocated() {
        // A set of allocations for KW including an allocation for an offender that expired one day ago and including
        // an active allocation for same offender that was assigned two hours ago.
        KeyWorkerAllocation expiredAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusDays(5));

        expiredAllocation = expireAllocation(expiredAllocation, null, LocalDateTime.now().minusDays(1));

        List<KeyWorkerAllocation> kwAllocations = Arrays.asList(
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 5, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 6, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 7, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 8, STAFF_ID),
                expiredAllocation,
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusHours(2))
        );

        // Expectations
        //  - repo to return a known Key worker with known number of active allocations
        //  - repo to return known set of KW allocations, including a recently expired allocation
        int expectedAllocationCount = 5;

        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.of(getKeyworker(STAFF_ID, expectedAllocationCount)));
        when(repo.getAllocationsForKeyworker(STAFF_ID)).thenReturn(kwAllocations);

        // Call service method
        Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);

        // Assertions - number of allocations should include active allocations only, not expired allocation because
        //              offender who is subject of expired allocation has been recalled and reallocated to same Key
        //              worker
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount);

        // Verifications
        verify(repo, atLeastOnce()).getKeyworkerDetails(eq(STAFF_ID));
        verify(repo, atLeastOnce()).getAllocationsForKeyworker(eq(STAFF_ID));
        verify(bookingService, never()).getLatestBookingByBookingId(anyLong());
    }

    // Given offender X is released
    // And their allocation to Key worker A is expired
    // And it is less than 48 hours since release of offender X
    // And offender X has an active booking in a different agency
    // When details are requested for Key worker A
    // Then number of allocations for Key worker A includes active allocations only
    //
    // If this test fails, a Key worker may appear to have less capacity than they should and may not be allocated
    // offenders when they normally would have been.
    @Test
    public void testGetKeyworkerDetailsWhenAllocationRecentlyExpiredButOffenderActiveElsewhere() {
        // A set of allocations for KW including an allocation for an offender that expired only 5 hours ago
        KeyWorkerAllocation expiredAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusDays(5));

        expiredAllocation = expireAllocation(expiredAllocation, null, LocalDateTime.now().minusHours(5));

        List<KeyWorkerAllocation> kwAllocations = Arrays.asList(
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 5, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 6, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 7, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 8, STAFF_ID),
                expiredAllocation
        );

        // Offender summary for offender who is subject of expired allocation
        OffenderSummary summary = getOffender(785L, "SYI", "A4565DF", true);

        // Expectations
        //  - repo to return a known Key worker with known number of active allocations
        //  - repo to return known set of KW allocations, including a recently expired allocation
        //  - booking service to return booking summary that indicates offender has active booking in different agency
        int expectedAllocationCount = 4;

        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.of(getKeyworker(STAFF_ID, expectedAllocationCount)));
        when(repo.getAllocationsForKeyworker(STAFF_ID)).thenReturn(kwAllocations);
        when(bookingService.getLatestBookingByBookingId(BOOKING_ID)).thenReturn(summary);

        // Call service method
        Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);

        // Assertions - number of allocations should include active allocations only, not expired allocation because
        //              offender who is subject of expired allocation now has an active booking at a different agency
        //              and is no longer eligible to be assigned to the same Key worker.
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount);

        // Verifications
        verify(repo, atLeastOnce()).getKeyworkerDetails(eq(STAFF_ID));
        verify(repo, atLeastOnce()).getAllocationsForKeyworker(eq(STAFF_ID));
        verify(bookingService, atLeastOnce()).getLatestBookingByBookingId(eq(BOOKING_ID));
    }

    // Given offender X is released
    // And their allocation to Key worker A is expired
    // And it is less than 48 hours since release of offender X
    // And offender X has an active booking in same agency as Key worker A
    // And offender X is unallocated
    // When details are requested for Key worker A
    // Then number of allocations for Key worker A includes all active allocations and expired allocation to offender X
    //
    // If this test fails, a Key worker may appear to have more capacity than they should and may be allocated
    // offenders when they normally would not have been.
    @Test
    public void testGetKeyworkerDetailsWhenAllocationRecentlyExpiredAndOffenderActiveButUnallocated() {
        // A set of allocations for KW including an allocation for an offender that expired over a day ago
        KeyWorkerAllocation expiredAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusDays(7));

        expiredAllocation = expireAllocation(expiredAllocation, null, LocalDateTime.now().minusHours(30));

        List<KeyWorkerAllocation> kwAllocations = Arrays.asList(
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 5, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 6, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 7, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 8, STAFF_ID),
                expiredAllocation
        );

        // Offender summary for offender who is subject of expired allocation
        OffenderSummary summary = getOffender(98745L, AGENCY_ID, "A9876BF", true);

        // Expectations
        //  - repo to return a known Key worker with known number of active allocations
        //  - repo to return known set of KW allocations, including a recently expired allocation
        //  - booking service to return booking summary that indicates offender has active booking in same agency
        //  - repo to return no active allocation to any Key worker for the offender (they are unallocated)
        int expectedAllocationCount = 4;

        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.of(getKeyworker(STAFF_ID, expectedAllocationCount)));
        when(repo.getAllocationsForKeyworker(STAFF_ID)).thenReturn(kwAllocations);
        when(bookingService.getLatestBookingByBookingId(BOOKING_ID)).thenReturn(summary);
        when(repo.getCurrentAllocationForOffenderBooking(summary.getBookingId())).thenReturn(Optional.empty());

        // Call service method
        Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);

        // Assertions - number of allocations should include active allocations plus the recently expired allocation
        //              because offender who is subject of recently expired allocation has an active booking in same
        //              agency but has not yet been allocated to another Key worker
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount + 1);

        // Verifications
        verify(repo, atLeastOnce()).getKeyworkerDetails(eq(STAFF_ID));
        verify(repo, atLeastOnce()).getAllocationsForKeyworker(eq(STAFF_ID));
        verify(bookingService, atLeastOnce()).getLatestBookingByBookingId(eq(BOOKING_ID));
        verify(repo, atLeastOnce()).getCurrentAllocationForOffenderBooking(eq(summary.getBookingId()));
    }

    // Given offender X is released
    // And their allocation to Key worker A is expired
    // And it is less than 48 hours since release of offender X
    // And offender X has an active booking in same agency as Key worker A
    // And offender X is allocated to a different Key worker
    // When details are requested for Key worker A
    // Then number of allocations for Key worker A includes all active allocations only
    //
    // If this test fails, a Key worker may appear to have less capacity than they should and may not be allocated
    // offenders when they normally would have been.
    @Test
    public void testGetKeyworkerDetailsWhenAllocationRecentlyExpiredAndOffenderAllocatedAnotherKeyworker() {
        // A set of allocations for KW including an allocation for an offender that expired a few hours ago and an
        // active allocation for same offender to different Key worker in same agency that was assigned an hour ago
        KeyWorkerAllocation expiredAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, BOOKING_ID, STAFF_ID, LocalDateTime.now().minusDays(7));

        expiredAllocation = expireAllocation(expiredAllocation, null, LocalDateTime.now().minusHours(9));

        Long newBookingId = 98754L;

        KeyWorkerAllocation newAllocation =
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, newBookingId, ANOTHER_STAFF_ID, LocalDateTime.now().minusHours(1));

        List<KeyWorkerAllocation> kwAllocations = Arrays.asList(
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 5, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 6, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 7, STAFF_ID),
                getPreviousKeyworkerAutoAllocation(AGENCY_ID, 8, STAFF_ID),
                expiredAllocation,
                newAllocation
        );

        // Offender summary for offender who is subject of expired allocation
        OffenderSummary summary = getOffender(newBookingId, AGENCY_ID, "A9876BF", true);

        // Expectations
        //  - repo to return a known Key worker with known number of active allocations
        //  - repo to return known set of KW allocations, including a recently expired allocation
        //  - booking service to return booking summary that indicates offender has active booking in same agency
        //  - repo to return no active allocation to any Key worker for the offender (they are unallocated)
        int expectedAllocationCount = 5;

        when(repo.getKeyworkerDetails(STAFF_ID)).thenReturn(Optional.of(getKeyworker(STAFF_ID, expectedAllocationCount)));
        when(repo.getAllocationsForKeyworker(STAFF_ID)).thenReturn(kwAllocations);
        when(bookingService.getLatestBookingByBookingId(BOOKING_ID)).thenReturn(summary);
        when(repo.getCurrentAllocationForOffenderBooking(summary.getBookingId())).thenReturn(Optional.of(newAllocation));

        // Call service method
        Keyworker keyworker = service.getKeyworkerDetails(STAFF_ID);

        // Assertions - number of allocations should include active allocations only, not recently expired allocation
        //              because offender who is subject of recently expired allocation is now allocated to a different
        //              Key worker.
        assertThat(keyworker.getNumberAllocated()).isEqualTo(expectedAllocationCount);

        // Verifications
        verify(repo, atLeastOnce()).getKeyworkerDetails(eq(STAFF_ID));
        verify(repo, atLeastOnce()).getAllocationsForKeyworker(eq(STAFF_ID));
        verify(bookingService, atLeastOnce()).getLatestBookingByBookingId(eq(BOOKING_ID));
        verify(repo, atLeastOnce()).getCurrentAllocationForOffenderBooking(eq(summary.getBookingId()));
    }
}
