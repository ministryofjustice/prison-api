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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.verifyException;
import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.verifyLog;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.fail;
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
    public void setup() {
        // Set-up mock appender to enable verification of log output
        KeyworkerTestHelper.initMockLogging(mockAppender);

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
    @Test
    public void testServicePerformsNoAllocationsWhenAllOffendersAreAllocated() {
        // No unallocated offenders
        when(keyWorkerAllocationService
                .getUnallocatedOffenders(anyString(), anyLong(), anyLong(), anyString(), any(Order.class)))
                .thenReturn(new Page<>(Collections.emptyList(), 0L, 0L, 10L));

        // Invoke auto-allocate
        allocationService.autoAllocate(TEST_AGENCY_ID);

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.never()).getAvailableKeyworkers(anyString());
        verify(keyWorkerAllocationService, Mockito.never()).allocate(any(NewAllocation.class));
        verifyLog(mockAppender, Level.INFO,AllocationService.OUTCOME_NO_UNALLOCATED_OFFENDERS);
    }

    // Given there are one or more offenders at an agency that are not allocated to a KW
    // And there are no KWs available for auto-allocation
    // When auto-allocation process is initiated
    // Then auto-allocation process does not perform any allocations
    // And auto-allocation process writes an error log entry with an appropriate message (to be defined)
    // And auto-allocation process throws an exception with an appropriate error message (to be defined)
    @Test
    public void testServiceErrorsWhenNoKeyWorkersAvailableForAutoAllocation() {
        // Some unallocated offenders
        Page<OffenderSummary> someUnallocatedOffenders = someUnallocatedOffenders(TEST_AGENCY_ID, 3L, 0L, 10L);

        when(keyWorkerAllocationService
                .getUnallocatedOffenders(anyString(), anyLong(), anyLong(), anyString(), any(Order.class)))
                .thenReturn(someUnallocatedOffenders);

        // No available Key workers
        when(keyWorkerAllocationService.getAvailableKeyworkers(anyString())).thenReturn(Collections.emptyList());

        // Invoke auto-allocate (catching expected exception)
        Throwable thrown = catchThrowable(() -> allocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);
        verify(keyWorkerAllocationService, Mockito.never()).allocate(any(NewAllocation.class));
        verifyLog(mockAppender, Level.ERROR, AllocationService.OUTCOME_NO_AVAILABLE_KEY_WORKERS);

        verifyException(thrown, AllocationException.class, AllocationService.OUTCOME_NO_AVAILABLE_KEY_WORKERS);
    }

    // Given there are one or more offenders at an agency that are not allocated to a KW
    // And there are KWs available for auto-allocation
    // And no unallocated offender has previously been allocated to any of the available Key workers
    // And all available KWs are fully allocated (have no capacity)
    // When auto-allocation process is initiated
    // Then auto-allocation process does not perform any allocations
    // And auto-allocation process writes an error log entry with an appropriate message (to be defined)
    // And auto-allocation process throws an exception with an appropriate error message (to be defined)
    @Test
    public void testServiceErrorsWhenNoKeyWorkersWithSpareAllocationCapacity() {
        final int allocCount = 9;

        // Some unallocated offenders
        Page<OffenderSummary> someUnallocatedOffenders = someUnallocatedOffenders(TEST_AGENCY_ID, 3L, 0L, 10L);

        when(keyWorkerAllocationService
                .getUnallocatedOffenders(anyString(), anyLong(), anyLong(), anyString(), any(Order.class)))
                .thenReturn(someUnallocatedOffenders);

        // Some available Key workers (at full capacity)
        List<Keyworker> someKeyworkers = KeyworkerTestHelper.getKeyworkers(3, allocCount, allocCount);

        when(keyWorkerAllocationService.getAvailableKeyworkers(anyString())).thenReturn(someKeyworkers);

        // A Key worker pool initialised with known capacity tier.
        KeyworkerPool keyworkerPool = new KeyworkerPool(someKeyworkers, Collections.singletonList(allocCount));

        when(keyworkerPoolFactory.getKeyworkerPool(someKeyworkers)).thenReturn(keyworkerPool);

        // No previous allocations between unallocated offenders and available Key workers
        when(keyWorkerAllocationService
                .getAllocationHistoryForPrisoner(anyLong(), anyString(), any(Order.class)))
                .thenReturn(Collections.emptyList());

        // Invoke auto-allocate (catching expected exception)
        Throwable thrown = catchThrowable(() -> allocationService.autoAllocate(TEST_AGENCY_ID));

        // Verify collaborator interactions and log output
        verify(keyWorkerAllocationService, Mockito.times(1))
                .getUnallocatedOffenders(eq(TEST_AGENCY_ID), eq(0L), eq(10L), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.times(1)).getAvailableKeyworkers(TEST_AGENCY_ID);

        verify(keyWorkerAllocationService, Mockito.times(1))
                .getAllocationHistoryForPrisoner(isLongBetween(1,3), anyString(), any(Order.class));

        verify(keyWorkerAllocationService, Mockito.never()).allocate(any(NewAllocation.class));
        verifyLog(mockAppender, Level.ERROR, AllocationService.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
        verifyException(thrown, AllocationException.class, AllocationService.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has been previously allocated to a KW at agency
    // And that KW is available for auto-allocation at same agency
    // When auto-allocation process is initiated
    // Then offender is allocated to same KW they were previously allocated to
    // And allocation is designated as an auto-allocation (to be confirmed)
    @Test @Ignore
    public void testOffenderAllocationToSameKeyWorkerPreviouslyAllocated() {
        fail("Not implemented.");
    }
    // Given an offender at an agency is not allocated to a KW
    // And offender has been previously allocated to multiple KWs at agency
    // And at least two of those KWs is available for auto-allocation at same agency
    // And all previous allocations for offender have occurred on different dates
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW they were most recently previously allocated to
    // And allocation is designated as an auto-allocation (to be confirmed)
    @Test @Ignore
    public void testOffenderAllocationToMostRecentKeyWorkerPreviouslyAllocated() {
        fail("Not implemented.");
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has had no previous KW allocation at the agency
    // And multiple, available KWs have capacity (current number of allocations less than Tier-1 capacity level)
    // And one KW has more capacity than any other KW
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW with most capacity
    // And allocation is designated as an auto-allocation
    @Test @Ignore
    public void testOffenderAllocationToKeyWorkerWithinTier1CapacityWithLeastAllocations() {
        fail("Not implemented.");
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
    @Test @Ignore
    public void testOffenderAllocationToKeyWorkerWithinTier1CapacityWithLeastAllocationsAndLeastRecentAllocation() {
        fail("Not implemented.");
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has had no previous KW allocation at the agency
    // And all available KWs are allocated at Tier-1 capacity level or higher
    // And multiple KWs have capacity (current number of allocations less than Tier-2 capacity level)
    // And one KW has more capacity than any other KW
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW with most capacity
    // And allocation is designated as an auto-allocation
    @Test @Ignore
    public void testOffenderAllocationToKeyWorkerWithinTier2CapacityWithLeastAllocations() {
        fail("Not implemented.");
    }

    // Given an offender at an agency is not allocated to a KW
    // And offender has had no previous KW allocation at the agency
    // And all available KWs are allocated at Tier-1 capacity level or higher
    // And multiple KWs have capacity (current number of allocations less than Tier-2 capacity level)
    // And all KWs have at least one auto-allocated offender
    // And all allocations have occurred on different dates
    // And at least two KWs have same capacity and more capacity than any other KW at agency
    // When auto-allocation process is initiated
    // Then offender is allocated to the KW with most capacity and the least recent auto-allocation
    // And allocation is designated as an auto-allocation
    @Test @Ignore
    public void testOffenderAllocationToKeyWorkerWithinTier2CapacityWithLeastAllocationsAndLeastRecentAllocation() {
        fail("Not implemented.");
    }

    // Provides page of unallocated offenders (consistent with supplied pagination parameters)
    private Page<OffenderSummary> someUnallocatedOffenders(String agencyId, long total, long offset, long limit) {
        List<OffenderSummary> unallocatedOffenders = new ArrayList<>();

        for (long i = (offset + 1); i <= Math.min(total, (offset + limit)); i++) {
            unallocatedOffenders.add(OffenderSummary.builder()
                    .bookingId(i)
                    .agencyLocationId(agencyId)
                    .build());
        }

        return new Page<>(unallocatedOffenders, total, offset, limit);
    }

    // Provides a previous Key worker allocation between specified offender and Key worker with specified properties
    private KeyWorkerAllocation previousKeyworkerAllocation(long bookingId, long staffId) {
        return KeyWorkerAllocation.builder()
                .bookingId(bookingId)
                .staffId(staffId)
                .build();
    }

    class IsLongBetween extends ArgumentMatcher<Long> {
        private final long lowerBound;
        private final long upperBound;

        public IsLongBetween(long lowerBound, long upperBound) {
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
