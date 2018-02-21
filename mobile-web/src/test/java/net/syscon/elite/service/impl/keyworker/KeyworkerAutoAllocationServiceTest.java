package net.syscon.elite.service.impl.keyworker;

import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.KeyWorkerAllocationRepository;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import net.syscon.elite.service.keyworker.KeyworkerAutoAllocationService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.buffer.BufferMetricReader;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static net.syscon.elite.service.impl.keyworker.KeyworkerAutoAllocationServiceImpl.COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS;
import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.*;

/**
 * Unit test for Key worker auto-allocation service.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyworkerAutoAllocationServiceTest {
    private static final String TEST_AGENCY_ID = "TST";

    private KeyworkerAutoAllocationService keyworkerAutoAllocationService;

    @Mock
    private KeyWorkerAllocationService keyWorkerAllocationService;

    @Mock
    private KeyworkerPoolFactory keyworkerPoolFactory;

    @Mock
    private KeyWorkerAllocationRepository repository;

    @Mock
    private AuthenticationFacade authenticationFacade;

    @Mock
    private BufferMetricReader metricReader;

    private long allocCount;

    @Before
    public void setUp() {
        // Initialise a counter service
        final CounterService counterService = new CounterService() {
            @Override
            public void increment(String metricName) {
                if (StringUtils.equals(metricName, COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS)) {
                    allocCount++;
                }
            }

            @Override
            public void decrement(String metricName) {
                if (StringUtils.equals(metricName, COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS)) {
                    allocCount--;
                }
            }

            @Override
            public void reset(String metricName) {
                if (StringUtils.equals(metricName, COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS)) {
                    allocCount = 0;
                }
            }
        };

        doAnswer((InvocationOnMock invocation) -> new Metric(COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS, allocCount))
                .when(metricReader).findOne(COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS);

        // Construct service under test (using mock collaborators)
        keyworkerAutoAllocationService =
                new KeyworkerAutoAllocationServiceImpl(keyWorkerAllocationService,
                        repository, keyworkerPoolFactory, authenticationFacade, counterService, metricReader);
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
        keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, never()).getAvailableKeyworkers(anyString());
        verify(repository, never()).createAllocation(any(KeyWorkerAllocation.class), anyString());
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
        Throwable thrown = catchThrowable(() -> keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(repository, never()).createAllocation(any(KeyWorkerAllocation.class), anyString());

        verifyException(thrown, AllocationException.class, KeyworkerAutoAllocationServiceImpl.OUTCOME_NO_AVAILABLE_KEY_WORKERS);
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
        Throwable thrown = catchThrowable(() -> keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, times(1))
                .getAllocationHistoryForPrisoner(isLongBetween(1,3), anyString(), any(Order.class));

        verify(repository, never()).createAllocation(any(KeyWorkerAllocation.class), anyString());
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
        mockKeyworkerRefresh(someKeyworkers);

        // Invoke auto-allocate
        keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, atLeastOnce())
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<KeyWorkerAllocation> kwaArg = ArgumentCaptor.forClass(KeyWorkerAllocation.class);

        verify(repository, times(1)).createAllocation(kwaArg.capture(), anyString());

        verifyAutoAllocation(kwaArg.getValue(), allocBookingId, allocStaffId);

        verify(keyWorkerAllocationService, times(1)).getKeyworkerDetails(allocStaffId);
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
        mockKeyworkerRefresh(someKeyworkers);

        // Invoke auto-allocate
        keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, atLeastOnce())
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<KeyWorkerAllocation> kwaArg = ArgumentCaptor.forClass(KeyWorkerAllocation.class);

        verify(repository, times(1)).createAllocation(kwaArg.capture(), anyString());

        verifyAutoAllocation(kwaArg.getValue(), allocBookingId, allocLaterStaffId);

        verify(keyWorkerAllocationService, times(1)).getKeyworkerDetails(allocLaterStaffId);
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
        mockKeyworkerRefresh(someKeyworkers);

        // Invoke auto-allocate
        keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, atLeastOnce())
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<KeyWorkerAllocation> kwaArg = ArgumentCaptor.forClass(KeyWorkerAllocation.class);

        verify(repository, times(1)).createAllocation(kwaArg.capture(), anyString());

        verifyAutoAllocation(kwaArg.getValue(), allocBookingId, leastAllocStaffId);

        verify(keyWorkerAllocationService, times(1)).getKeyworkerDetails(eq(leastAllocStaffId));
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
        mockKeyworkerRefresh(someKeyworkers);

        // Invoke auto-allocate
        keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, atLeastOnce())
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, times(1))
                .getAllocationHistoryForPrisoner(eq(1L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(2))
                .getAllocationsForKeyworker(anyLong());

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<KeyWorkerAllocation> kwaArg = ArgumentCaptor.forClass(KeyWorkerAllocation.class);

        verify(repository, times(1)).createAllocation(kwaArg.capture(), anyString());

        verifyAutoAllocation(kwaArg.getValue(), allocBookingId, olderLeastAllocStaffId);

        verify(keyWorkerAllocationService, times(1)).getKeyworkerDetails(eq(olderLeastAllocStaffId));
    }

    // Given multiple pages (page size = 10) of offenders at an agency are not allocated to a KW
    // And offenders have no previous KW allocation at the agency
    // And multiple, available KWs have capacity
    // And total capacity, across all KWs, is sufficient to allow all offenders to be allocated
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
        mockKeyworkerRefresh(someKeyworkers);

        // Invoke auto-allocate
        keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, atLeast(totalOffenders / pageSize.intValue()))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(pageSize), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, times(totalOffenders))
                .getAllocationHistoryForPrisoner(isLongBetween(1L, totalOffenders), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<KeyWorkerAllocation> kwaArg = ArgumentCaptor.forClass(KeyWorkerAllocation.class);

        verify(repository, times(totalOffenders)).createAllocation(kwaArg.capture(), anyString());

        kwaArg.getAllValues().forEach(kwAlloc -> {
            assertThat(kwAlloc.getBookingId()).isBetween(1L, totalOffenders.longValue());
            assertThat(kwAlloc.getStaffId()).isBetween(1L, totalKeyworkers.longValue());
            assertThat(kwAlloc.getType()).isEqualTo(AllocationType.AUTO.getIndicator());
            assertThat(kwAlloc.getReason()).isEqualTo(KeyworkerAutoAllocationService.ALLOCATION_REASON_AUTO);
        });

        verify(keyWorkerAllocationService, times(totalOffenders)).getKeyworkerDetails(isLongBetween(1L, totalKeyworkers));
    }

    // Given multiple pages (page size = 10) of offenders at an agency are not allocated to a KW
    // And offenders have no previous KW allocation at the agency
    // And multiple, available KWs have capacity
    // And total capacity, across all KWs, is not sufficient to allow all offenders to be allocated
    // When auto-allocation process is initiated
    // Then offenders are allocated to a KW whilst there is capacity
    // And all successful allocations are designated as auto-allocations
    // And auto-allocation process writes an error log entry with an appropriate message (to be defined)
    // And auto-allocation process throws an exception with an appropriate error message (to be defined)
    //
    // If this test fails, auto-allocation may fail to allocate some offenders when there is capacity.
    @Test
    public void testSomeOffendersAllocatedBeforeErrorDueToNoCapacity() {
        // Multple pages of unallocated offenders (25 offenders = 3 pages when page size is 10)
        Integer totalOffenders = 25;
        Integer totalKeyworkers = 5;
        Long pageSize = 10L;

        mockUnallocatedOffenders(TEST_AGENCY_ID, totalOffenders, pageSize);

        // Some available Key workers with some capacity but not enough total capacity to allocate all offenders
        List<Keyworker> someKeyworkers = mockKeyworkers(totalKeyworkers,FULLY_ALLOCATED - 2, FULLY_ALLOCATED);

        // Determine available capacity
        int totalCapacity = (totalKeyworkers * FULLY_ALLOCATED) -
                someKeyworkers.stream().mapToInt(Keyworker::getNumberAllocated).sum();

        // A Key worker pool initialised with known capacity tier.
        mockKeyworkerPool(someKeyworkers);

        // No previous allocations between any offender and available Key workers
        mockPrisonerAllocationHistory();

        // Mock KW refresh following allocation
        mockKeyworkerRefresh(someKeyworkers);

        // Invoke auto-allocate (catching expected exception)
        Throwable thrown = catchThrowable(() -> keyworkerAutoAllocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, atLeastOnce())
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(pageSize), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyworkerPoolFactory, times(1)).getKeyworkerPool(someKeyworkers);

        verify(keyWorkerAllocationService, atLeast(totalCapacity))
                .getAllocationHistoryForPrisoner(isLongBetween(1L, totalOffenders), anyString(), any(Order.class));

        // Expecting allocation to succeed - verify request includes expected values
        ArgumentCaptor<KeyWorkerAllocation> kwaArg = ArgumentCaptor.forClass(KeyWorkerAllocation.class);

        verify(repository, times(totalCapacity)).createAllocation(kwaArg.capture(), anyString());

        kwaArg.getAllValues().forEach(kwAlloc -> {
            assertThat(kwAlloc.getBookingId()).isBetween(1L, totalOffenders.longValue());
            assertThat(kwAlloc.getStaffId()).isBetween(1L, totalKeyworkers.longValue());
            assertThat(kwAlloc.getType()).isEqualTo(AllocationType.AUTO.getIndicator());
            assertThat(kwAlloc.getReason()).isEqualTo(KeyworkerAutoAllocationService.ALLOCATION_REASON_AUTO);
        });

        verify(keyWorkerAllocationService, times(totalCapacity)).getKeyworkerDetails(isLongBetween(1L, totalKeyworkers));

        verifyException(thrown, AllocationException.class, KeyworkerPool.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
    }

    private void mockUnallocatedOffenders(String agencyId, long total, long limit) {
        Answer unallocOffendersAnswer = new Answer() {
            private final long originalTotal = total;
            Page<OffenderSummary> pagedUnallocatedOffenders;
            private long totalRemaining = total;

            public Object answer(InvocationOnMock invocation) {
                if (totalRemaining > 0) {
                    pagedUnallocatedOffenders =
                            pagedUnallocatedOffenders(agencyId, totalRemaining, originalTotal - totalRemaining + 1, limit);

                    totalRemaining -= limit;
                } else {
                    pagedUnallocatedOffenders = new Page<>(Collections.emptyList(), 0, 0, limit);
                }

                return pagedUnallocatedOffenders;
            }
        };

        doAnswer(unallocOffendersAnswer).when(keyWorkerAllocationService)
                .getUnallocatedOffenders(eq(agencyId), anyLong(), anyLong(), anyString(), any(Order.class));
    }

    // Provides page of unallocated offenders (consistent with supplied pagination parameters)
    private Page<OffenderSummary> pagedUnallocatedOffenders(String agencyId, long total, long startId, long limit) {
        List<OffenderSummary> unallocatedOffenders = new ArrayList<>();

        for (long i = 0; i < Math.min(total, limit); i++) {
            unallocatedOffenders.add(getOffender(startId + i, agencyId));
        }

        return new Page<>(unallocatedOffenders, total, 0L, limit);
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

    private void mockKeyworkerRefresh(List<Keyworker> keyworkers) {
        doAnswer(new Answer() {
            // Tracks initial set of Key workers and their allocation counts so increase in allocation count can be
            // simulated when an offender is allocated to a Key worker.
            final Map<Long, Integer> keyworkerAllocs =
                    keyworkers.stream().collect(Collectors.toMap(Keyworker::getStaffId, Keyworker::getNumberAllocated));

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Long staffId = (Long) invocation.getArguments()[0];
                int newAllocCount = keyworkerAllocs.get(staffId) + 1;
                keyworkerAllocs.put(staffId, newAllocCount);

                return getKeyworker(staffId, newAllocCount);
            }
        }).when(keyWorkerAllocationService).getKeyworkerDetails(anyLong());
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
