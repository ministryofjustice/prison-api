package net.syscon.elite.service.impl.keyworker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import net.syscon.elite.service.keyworker.AllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for Key worker auto-allocation service.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoAllocationServiceTest {
    private static final String TEST_AGENCY_ID = "TST";

    private AllocationService allocationService;

    @Mock
    private KeyWorkerAllocationService keyWorkerAllocationService;

    @Mock
    private KeyworkerPoolFactory keyworkerPoolFactory;

    @Mock
    private Appender mockAppender;

    @Before
    public void setUp() {
        // Set-up mock appender to enable verification of log output
        initMockLogging(mockAppender);

        // Construct service under test (using mock collaborators)
        allocationService = new AutoAllocationServiceImpl(keyWorkerAllocationService, keyworkerPoolFactory);
    }

    // Each unit test below is preceded by acceptance criteria in Given-When-Then form
    // KW = Key worker
    // Available refers to a staff member having the KW role at the agency and being available (i.e. not inactive or unavailable)
    // Capacity refers to spare allocation capacity (i.e. the KW has capacity for further offender allocations)
    // Allocation refers to an extant and active relationship of an offender to a Key worker
    //   (there is a distinction between an automatically created allocation and a manually created allocation)
    // For purposes of these tests, 'multiple' means at least three or more

    // Given that all offenders at an agency are allocated to a KW
    // When auto-allocation process is initiated
    // Then auto-allocation process does not perform any allocations
    // And auto-allocation process writes an informational log entry with an appropriate message (to be defined)
    //
    // If this test fails, automatic allocation may be attempted for an offender that is already allocated.
    @Test
    public void testServicePerformsNoAllocationsWhenAllOffendersAreAllocated() {
        // No unallocated offenders
        mockUnallocatedOffenders(TEST_AGENCY_ID, 0, 10);

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.never()).getAvailableKeyworkers(anyString());
        verify(keyWorkerAllocationService, Mockito.never()).allocate(any(NewAllocation.class));
        verifyLog(mockAppender, Level.INFO, AutoAllocationServiceImpl.OUTCOME_NO_UNALLOCATED_OFFENDERS);
    }

    // Given there are one or more offenders at an agency that are not allocated to a KW
    // And there are no KWs available for auto-allocation
    // When auto-allocation process is initiated
    // Then auto-allocation process does not perform any allocations
    // And auto-allocation process writes an error log entry with an appropriate message (to be defined)
    // And auto-allocation process throws an exception with an appropriate error message (to be defined)
    //
    // If this test fails, offenders may be allocated to Key workers that are not available for allocation.
    @Test
    public void testServiceErrorsWhenNoKeyWorkersAvailableForAutoAllocation() {
        // Some unallocated offenders
        mockUnallocatedOffenders(TEST_AGENCY_ID, 3, 10);

        // No available Key workers
        mockKeyworkers(0, 0, 0);

        // Invoke auto-allocate (catching expected exception)
        Throwable thrown = catchThrowable(() -> allocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyWorkerAllocationService, Mockito.never()).allocate(any(NewAllocation.class));
        verifyLog(mockAppender, Level.ERROR, AutoAllocationServiceImpl.OUTCOME_NO_AVAILABLE_KEY_WORKERS);

        verifyException(thrown, AllocationException.class, AutoAllocationServiceImpl.OUTCOME_NO_AVAILABLE_KEY_WORKERS);
    }

    // Given there are one or more offenders at an agency that are not allocated to a KW
    // And there are KWs available for auto-allocation
    // And no unallocated offender has previously been allocated to any of the available Key workers
    // And all available KWs are fully allocated (have no capacity)
    // When auto-allocation process is initiated
    // Then auto-allocation process does not perform any allocations
    // And auto-allocation process writes an error log entry with an appropriate message (to be defined)
    // And auto-allocation process throws an exception with an appropriate error message (to be defined)
    //
    // If this test fails, Key workers may be allocated too many offenders.
    @Test
    public void testServiceErrorsWhenNoKeyWorkersWithSpareAllocationCapacity() {
        // Some unallocated offenders
        mockUnallocatedOffenders(TEST_AGENCY_ID, 3, 10);

        // Some available Key workers (at full capacity)
        List<Keyworker> someKeyworkers = mockKeyworkers(3, FULLY_ALLOCATED, FULLY_ALLOCATED);

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // No previous allocations between unallocated offenders and available Key workers
        mockPrisonerAllocationHistory();

        // Invoke auto-allocate (catching expected exception)
        Throwable thrown = catchThrowable(() -> allocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, Mockito.times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, Mockito.times(1))
                .getAllocationHistoryForPrisoner(isLongBetween(1,3), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.never()).allocate(any(NewAllocation.class));
        verifyLog(mockAppender, Level.ERROR, KeyworkerPool.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
        verifyException(thrown, AllocationException.class, KeyworkerPool.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
    }

    // Given an offender at an agency is not allocated to a KW
    // And there are KWs available for auto-allocation
    // And offender has previously been allocated to one of the available KWs
    // And that KW has least capacity of all available KWs (i.e. it would not normally be next in line for allocation)
    // When auto-allocation process is initiated
    // Then offender is allocated to same KW they were previously allocated to
    // And allocation is designated as an auto-allocation
    //
    // If this test fails, an offender will not be allocated to a Key worker they were previously allocated to.
    @Test
    public void testOffenderAllocationToSameKeyWorkerPreviouslyAllocated() {
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long allocBookingId = 1;
        final long allocStaffId = 2;

        // An unallocated offender
        mockUnallocatedOffenders(TEST_AGENCY_ID, 1, 10);

        // Some available Key workers (with known capacities)
        Keyworker previousKeyworker = getKeyworker(allocStaffId, highAllocCount);

        List<Keyworker> someKeyworkers = mockKeyworkers(
                getKeyworker(1, lowAllocCount),
                previousKeyworker,
                getKeyworker(3, lowAllocCount));

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // A previous allocation between the unallocated offender and Key worker with staffId = 2
        KeyWorkerAllocation previousAllocation = getPreviousKeyworkerAllocation(allocBookingId, allocStaffId);

        mockPrisonerAllocationHistory(previousAllocation);

        // Mock KW refresh following allocation
        mockKeyworkerRefresh(previousKeyworker);

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, Mockito.times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, Mockito.times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<NewAllocation> newAllocArg = ArgumentCaptor.forClass(NewAllocation.class);

        verify(keyWorkerAllocationService, Mockito.times(1)).allocate(newAllocArg.capture());

        verifyAutoAllocation(newAllocArg.getValue(), allocBookingId, allocStaffId);

        verifyLog(mockAppender, Level.INFO, AutoAllocationServiceImpl.OUTCOME_AUTO_ALLOCATION_SUCCESS, allocBookingId);

        verify(keyWorkerAllocationService, Mockito.times(1)).getKeyworkerDetails(allocStaffId);
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has previously been allocated to multiple KWs at agency
    // And at least two of those KWs are available for auto-allocation at same agency
    // And all previous allocations for offender have occurred on different dates
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW they were most recently previously allocated to
    // And allocation is designated as an auto-allocation
    //
    // If this test fails, an offender will not be allocated to the Key worker they were most recently allocated to.
    @Test
    public void testOffenderAllocationToMostRecentKeyWorkerPreviouslyAllocated() {
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long allocBookingId = 1;
        final long allocEarlierStaffId = 2;
        final long allocLaterStaffId = 4;

        // An unallocated offender
        mockUnallocatedOffenders(TEST_AGENCY_ID, 1, 10);

        // Some available Key workers (with known capacities)
        Keyworker earlierKeyworker = getKeyworker(allocEarlierStaffId, lowAllocCount);
        Keyworker laterKeyworker = getKeyworker(allocLaterStaffId, highAllocCount);

        List<Keyworker> someKeyworkers = mockKeyworkers(
                getKeyworker(1, lowAllocCount + 1),
                laterKeyworker,
                getKeyworker(3, lowAllocCount + 2),
                earlierKeyworker);

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // Previous allocations between the unallocated offender and different Key workers at different date/times
        LocalDateTime assignedEarlier = LocalDateTime.now().minusMonths(9);
        LocalDateTime assignedLater = assignedEarlier.plusMonths(3);
        KeyWorkerAllocation prevEarlierAllocation = getPreviousKeyworkerAllocation(allocBookingId, allocEarlierStaffId, assignedEarlier);
        KeyWorkerAllocation prevLaterAllocation = getPreviousKeyworkerAllocation(allocBookingId, allocLaterStaffId, assignedLater);

        mockPrisonerAllocationHistory(prevEarlierAllocation, prevLaterAllocation);

        // Mock KW refresh following allocation
        mockKeyworkerRefresh(laterKeyworker);

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, Mockito.times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, Mockito.times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<NewAllocation> newAllocArg = ArgumentCaptor.forClass(NewAllocation.class);

        verify(keyWorkerAllocationService, Mockito.times(1)).allocate(newAllocArg.capture());

        verifyAutoAllocation(newAllocArg.getValue(), allocBookingId, allocLaterStaffId);

        verifyLog(mockAppender, Level.INFO, AutoAllocationServiceImpl.OUTCOME_AUTO_ALLOCATION_SUCCESS, allocBookingId);

        verify(keyWorkerAllocationService, Mockito.times(1)).getKeyworkerDetails(allocLaterStaffId);
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has had no previous KW allocation at the agency
    // And multiple, available KWs have capacity (current number of allocations less than Tier-1 capacity level)
    // And one KW has more capacity than any other KW
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW with most capacity
    // And allocation is designated as an auto-allocation
    //
    // If this test fails, an offender will not be auto-allocated to the offender with most capacity.
    @Test
    public void testOffenderAllocationToKeyWorkerWithinTier1CapacityWithLeastAllocations() {
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long allocBookingId = 1;
        final long leastAllocStaffId = 3;

        // An unallocated offender
        mockUnallocatedOffenders(TEST_AGENCY_ID, 1, 10);

        // Some available Key workers (with known capacities)
        Keyworker leastAllocKeyworker = getKeyworker(leastAllocStaffId, lowAllocCount);

        List<Keyworker> someKeyworkers = mockKeyworkers(
                getKeyworker(1, highAllocCount),
                getKeyworker(2, highAllocCount),
                leastAllocKeyworker);

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // No previous allocations between unallocated offender and available Key workers
        mockPrisonerAllocationHistory();

        // Mock KW refresh following allocation
        mockKeyworkerRefresh(leastAllocKeyworker);

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, Mockito.times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, Mockito.times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<NewAllocation> newAllocArg = ArgumentCaptor.forClass(NewAllocation.class);

        verify(keyWorkerAllocationService, Mockito.times(1)).allocate(newAllocArg.capture());

        verifyAutoAllocation(newAllocArg.getValue(), allocBookingId, leastAllocStaffId);

        verifyLog(mockAppender, Level.INFO, AutoAllocationServiceImpl.OUTCOME_AUTO_ALLOCATION_SUCCESS, allocBookingId);

        verify(keyWorkerAllocationService, Mockito.times(1)).getKeyworkerDetails(eq(leastAllocStaffId));
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has had no previous KW allocation at the agency
    // And multiple, available KWs have capacity (current number of allocations less than Tier-1 capacity level)
    // And all KWs have at least one auto-allocated offender
    // And all allocations have occurred on different dates
    // And at least two KWs have same capacity and more capacity than any other KW at agency
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW with most capacity and the least recent auto-allocation
    // And allocation is designated as an auto-allocation
    //
    // If this test fails, an offender will not be auto-allocated to the offender with most capacity and least recent
    // auto-allocation.
    @Test
    public void testOffenderAllocationToKeyWorkerWithinTier1CapacityWithLeastAllocationsAndLeastRecentAllocation() {
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long allocBookingId = 1;
        final long recentLeastAllocStaffId = 3;
        final long olderLeastAllocStaffId = 4;

        // An unallocated offender
        mockUnallocatedOffenders(TEST_AGENCY_ID, 1, 10);

        // Some available Key workers (with known capacities)
        Keyworker recentLeastAllocKeyworker = getKeyworker(recentLeastAllocStaffId, lowAllocCount);
        Keyworker olderLeastAllocKeyworker = getKeyworker(olderLeastAllocStaffId, lowAllocCount);

        List<Keyworker> someKeyworkers = mockKeyworkers(
                getKeyworker(1, highAllocCount),
                getKeyworker(2, highAllocCount),
                recentLeastAllocKeyworker,
                olderLeastAllocKeyworker);

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // No previous allocations between unallocated offender and available Key workers
        mockPrisonerAllocationHistory();

        // Some previous auto-allocations for Key workers of interest
        LocalDateTime refDateTime = LocalDateTime.now();

        KeyWorkerAllocation recentAllocation =
                getPreviousKeyworkerAllocation(5, recentLeastAllocStaffId, refDateTime.minusDays(2));

        KeyWorkerAllocation olderAllocation =
                getPreviousKeyworkerAllocation(7, olderLeastAllocStaffId, refDateTime.minusDays(7));

        mockKeyworkerAllocationHistory(recentLeastAllocStaffId, recentAllocation);
        mockKeyworkerAllocationHistory(olderLeastAllocStaffId, olderAllocation);

        // Mock KW refresh following allocation
        mockKeyworkerRefresh(olderLeastAllocKeyworker);

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, Mockito.times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, Mockito.times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(2))
                .getAllocationsForKeyworker(anyLong());

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<NewAllocation> newAllocArg = ArgumentCaptor.forClass(NewAllocation.class);

        verify(keyWorkerAllocationService, Mockito.times(1)).allocate(newAllocArg.capture());

        verifyAutoAllocation(newAllocArg.getValue(), allocBookingId, olderLeastAllocStaffId);

        verifyLog(mockAppender, Level.INFO, AutoAllocationServiceImpl.OUTCOME_AUTO_ALLOCATION_SUCCESS, allocBookingId);

        verify(keyWorkerAllocationService, Mockito.times(1)).getKeyworkerDetails(eq(olderLeastAllocStaffId));
    }

    // Given multiple pages (page size = 10) of offenders at an agency are not allocated to a KW
    // And offenders have no previous KW allocation at the agency
    // And multiple, available KWs have capacity
    // And total capacity, across all Kws, is sufficient to allow all offenders to be allocated
    // When auto-allocation process is initiated
    // Then all offenders are allocated to a KW
    // And all allocations are designated as auto-allocations
    //
    // If this test fails, auto-allocation of multiple offenders may not complete successfully.
    @Test
    public void testAllOffendersAllocated() {
        // Multple pages of unallocated offenders (25 offenders = 3 pages when page size is 10)
        Integer totalOffenders = 25;
        Integer totalKeyworkers = 5;
        Long pageSize = 10L;

        mockUnallocatedOffenders(TEST_AGENCY_ID, totalOffenders, pageSize);

        // Enough available Key workers (with enough total capacity to allocate all offenders)
        List<Keyworker> someKeyworkers = mockKeyworkers(totalKeyworkers, 0, 0);

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // No previous allocations between any offender and available Key workers
        mockPrisonerAllocationHistory();

        // Mock KW refresh following allocation
        someKeyworkers.forEach(this::mockKeyworkerRefresh);

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        for (long i = 0; i < totalOffenders; i += pageSize) {
            verify(keyWorkerAllocationService, Mockito.times(1))
                    .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(i), eq(pageSize), anyString(), any(Order.class));
        }

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, Mockito.times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, Mockito.times(totalOffenders))
                .getAllocationHistoryForPrisoner(isLongBetween(1L, totalOffenders), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<NewAllocation> newAllocArg = ArgumentCaptor.forClass(NewAllocation.class);

        verify(keyWorkerAllocationService, Mockito.times(totalOffenders)).allocate(newAllocArg.capture());

        newAllocArg.getAllValues().forEach(newAlloc -> {
            assertThat(newAlloc.getBookingId()).isBetween(1L, totalOffenders.longValue());
            assertThat(newAlloc.getStaffId()).isBetween(1L, totalKeyworkers.longValue());
            assertThat(newAlloc.getType()).isEqualTo(AllocationType.AUTO.getIndicator());
            assertThat(newAlloc.getReason()).isEqualTo(AllocationService.ALLOCATION_REASON_AUTO);
        });

        verify(keyWorkerAllocationService, Mockito.times(totalOffenders)).getKeyworkerDetails(isLongBetween(1L, totalKeyworkers));
    }

    private void mockUnallocatedOffenders(String agencyId, long total, long limit) {
        Page<OffenderSummary> pagedUnallocatedOffenders;

        if (total > 0) {
            for(long i = 0; i < total; i += limit) {
                pagedUnallocatedOffenders = pagedUnallocatedOffenders(agencyId, total, i, limit);

                when(keyWorkerAllocationService
                        .getUnallocatedOffenders(eq(agencyId), eq(i), eq(limit), anyString(), any(Order.class)))
                        .thenReturn(pagedUnallocatedOffenders);
            }
        } else {
            pagedUnallocatedOffenders = new Page<>(Collections.emptyList(), 0, 0, limit);

            when(keyWorkerAllocationService
                    .getUnallocatedOffenders(eq(agencyId), anyLong(), anyLong(), anyString(), any(Order.class)))
                    .thenReturn(pagedUnallocatedOffenders);
        }
    }

    // Provides page of unallocated offenders (consistent with supplied pagination parameters)
    private Page<OffenderSummary> pagedUnallocatedOffenders(String agencyId, long total, long offset, long limit) {
        List<OffenderSummary> unallocatedOffenders = new ArrayList<>();

        for (long i = (offset + 1); i <= Math.min(total, (offset + limit)); i++) {
            unallocatedOffenders.add(getOffender(i, agencyId));
        }

        return new Page<>(unallocatedOffenders, total, offset, limit);
    }

    private List<Keyworker> mockKeyworkers(long total, int minAllocations, int maxAllocations) {
        List<Keyworker> mockKeyworkers;

        if (total > 0) {
            mockKeyworkers = getKeyworkers(total, minAllocations, maxAllocations);
        } else {
            mockKeyworkers = Collections.emptyList();
        }

        when(keyWorkerAllocationService.getAvailableKeyworkers(anyString())).thenReturn(mockKeyworkers);

        return mockKeyworkers;
    }

    private List<Keyworker> mockKeyworkers(Keyworker... keyworkers) {
        List<Keyworker> mockKeyworkers;

        if (keyworkers.length > 0) {
            mockKeyworkers = Arrays.asList(keyworkers);
        } else {
            mockKeyworkers = Collections.emptyList();
        }

        when(keyWorkerAllocationService.getAvailableKeyworkers(anyString())).thenReturn(mockKeyworkers);

        return mockKeyworkers;
    }

    private void mockKeyworkerPool(List<Keyworker> keyworkers) {
        KeyworkerPool keyworkerPool = new KeyworkerPool(keyworkers, Collections.singletonList(FULLY_ALLOCATED));

        keyworkerPool.setKeyWorkerAllocationService(keyWorkerAllocationService);

        when(keyworkerPoolFactory.getKeyworkerPool(keyworkers)).thenReturn(keyworkerPool);
    }

    private void mockPrisonerAllocationHistory(KeyWorkerAllocation... allocations) {
        List<KeyWorkerAllocation> allocationHistory =
                (allocations == null) ? Collections.emptyList() : Arrays.asList(allocations);

        when(keyWorkerAllocationService
                .getAllocationHistoryForPrisoner(anyLong(), anyString(), any(Order.class)))
                .thenReturn(allocationHistory);
    }

    private void mockKeyworkerAllocationHistory(Long staffId, KeyWorkerAllocation... allocations) {
        List<KeyWorkerAllocation> allocationHistory =
                (allocations == null) ? Collections.emptyList() : Arrays.asList(allocations);

        when(keyWorkerAllocationService.getAllocationsForKeyworker(eq(staffId))).thenReturn(allocationHistory);
    }

    private void mockKeyworkerRefresh(Keyworker keyworker) {
        Keyworker refreshedKeyworker = getKeyworker(keyworker.getStaffId(), keyworker.getNumberAllocated() + 1);

        when(keyWorkerAllocationService.getKeyworkerDetails(eq(keyworker.getStaffId()))).thenReturn(refreshedKeyworker);
    }

    class IsLongBetween extends ArgumentMatcher<Long> {
        private final long lowerBound;
        private final long upperBound;

        IsLongBetween(long lowerBound, long upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        @Override
        public boolean matches(Object argument) {
            long argVal = (Long) argument;

            return (argVal >= lowerBound) && (argVal <= upperBound);
        }
    }

    private long isLongBetween(long lowerBound, long upperBound) {
        return longThat(new IsLongBetween(lowerBound, upperBound));
    }
}
