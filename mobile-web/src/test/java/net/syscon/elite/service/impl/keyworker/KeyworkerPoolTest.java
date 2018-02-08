package net.syscon.elite.service.impl.keyworker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.*;

import static net.syscon.elite.service.impl.keyworker.KeyworkerTestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for Key worker pool.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyworkerPoolTest {
    private KeyworkerPool keyworkerPool;
    private Set<Integer> capacityTiers;

    @Mock
    private KeyWorkerAllocationService keyWorkerAllocationService;

    @Mock
    private Appender mockAppender;

    @Before
    public void setUp() {
        // Initialise Key worker allocation capacity tiers
        capacityTiers = new TreeSet<>();

        capacityTiers.add(CAPACITY_TIER_1);
        capacityTiers.add(CAPACITY_TIER_2);

        // Set-up mock appender to enable verification of log output
        KeyworkerTestHelper.initMockLogging(mockAppender);
    }

    // Each unit test below is preceded by acceptance criteria in Given-When-Then form
    // KW = Key worker
    // KWP = Key worker pool
    // Capacity refers to spare allocation capacity (i.e. the KW has capacity for further offender allocations)
    // Allocation refers to an extant and active relationship of an offender to a Key worker
    //   (there is a distinction between an automatically created allocation and a manually created allocation)
    // For purposes of these tests, 'multiple' means at least three or more

    // Given an offender is seeking KW allocation
    // And offender has never previously been allocated to a KW
    // And there is a single KW in the KWP
    // And that KW has capacity
    // When KWP requested for KW for offender
    // Then single KW in KWP is returned
    //
    // If this test fails, an offender will not be allocated to a Key worker.
    @Test
    public void testSingleKeyworkerWithCapacity() {
        // Single KW, with capacity, in KWP
        Keyworker keyworker = getKeyworker(1, CAPACITY_TIER_1);
        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, Collections.singleton(keyworker), capacityTiers);

        // Request KW from pool for offender
        Keyworker allocatedKeyworker = keyworkerPool.getKeyworker(1);

        // Verify same KW used to initialise pool is returned
        assertThat(allocatedKeyworker).isSameAs(keyworker);
    }

    // Given an offender is seeking KW allocation
    // And offender has never previously been allocated to a KW
    // And there is a single KW in the KWP
    // And that KW is fully allocated (has no capacity)
    // When KWP requested for KW for offender
    // Then an error is logged with message to effect that all Key workers are fully allocated
    // And an AllocationException is thrown with message to effect that all Key workers are fully allocated
    //
    // If this test fails, a Key worker will be allocated too many offenders.
    @Test
    public void testPoolErrorsWhenSingleKeyworkerIsFullyAllocated() {
        // Single KW, fully allocated, in KWP
        Keyworker keyworker = getKeyworker(1, FULLY_ALLOCATED);
        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, Collections.singleton(keyworker), capacityTiers);

        // Request KW from pool (catching expected exception)
        Throwable thrown = catchThrowable(() -> keyworkerPool.getKeyworker(1));

        // Verify log output and exception
        KeyworkerTestHelper.verifyLog(mockAppender, Level.ERROR, KeyworkerPool.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);

        assertThat(thrown)
                .isInstanceOf(AllocationException.class)
                .hasMessage(KeyworkerPool.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
    }

    // Given an offender is seeking KW allocation
    // And offender has never previously been allocated to a KW
    // And there are multiple KWs in the KWP
    // And all KWs have differing capacity (but none are fully allocated)
    // When KWP requested for KW offender
    // Then KW with most capacity is returned
    //
    // If this test fails, offenders may not be allocated evenly across Key workers.
    @Test
    public void testKeyworkerWithMostCapacityIsReturned() {
        // Multiple KWs, all with capacity, in KWP
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        List<Keyworker> keyworkers = getKeyworkers(3, lowAllocCount, highAllocCount);
        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, keyworkers, capacityTiers);

        // Request KW from pool for offender
        Keyworker allocatedKeyworker = keyworkerPool.getKeyworker(1);

        // Verify returned KW is the one with fewest allocations
        OptionalInt fewestAllocs = keyworkers.stream().mapToInt(Keyworker::getNumberAllocated).min();

        assertThat(allocatedKeyworker.getNumberAllocated()).isEqualTo(fewestAllocs.orElse(-1));
    }

    // Given an offender is seeking KW allocation
    // And offender has never previously been allocated to a KW
    // And there are multiple KWs in the KWP
    // And multiple KWs have most capacity (i.e. same number of allocations and more allocations than other KWs)
    // And all KWs have existing auto-allocations assigned at different date/times
    // When KWP requested for KW offender
    // Then KW with most capacity and least recent auto-allocation is returned
    //
    // If this test fails, offenders may not be allocated to Key worker with most capacity and least-recent auto-allocation.
    @Test
    public void testKeyworkerWithMostCapacityAndLeastRecentAllocationIsReturned() {
        // Multiple KWs, all with capacity and a couple with same least number of allocations, in KWP
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long staffId1 = 1L;
        final long staffId2 = 2L;
        final long staffId3 = 3L;

        List<Keyworker> keyworkers = Arrays.asList(
                getKeyworker(1, lowAllocCount),
                getKeyworker(2, highAllocCount),
                getKeyworker(3, lowAllocCount));

        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, keyworkers, capacityTiers);

        // Some previous allocations for each Key worker
        LocalDateTime refDateTime = LocalDateTime.now();

        KeyWorkerAllocation staff1Allocation =
                getPreviousKeyworkerAllocation(5, staffId1, refDateTime.minusDays(2));

        KeyWorkerAllocation staff2Allocation =
                getPreviousKeyworkerAllocation(7, staffId2, refDateTime.minusDays(7));

        KeyWorkerAllocation staff3Allocation =
                getPreviousKeyworkerAllocation(3, staffId3, refDateTime.minusDays(5));

        when(keyWorkerAllocationService.getAllocationsForKeyworker(eq(staffId1))).thenReturn(Collections.singletonList(staff1Allocation));
        when(keyWorkerAllocationService.getAllocationsForKeyworker(eq(staffId2))).thenReturn(Collections.singletonList(staff2Allocation));
        when(keyWorkerAllocationService.getAllocationsForKeyworker(eq(staffId3))).thenReturn(Collections.singletonList(staff3Allocation));

        // Request KW from pool for offender
        Keyworker allocatedKeyworker = keyworkerPool.getKeyworker(1);

        // Verify collaborators
        verify(keyWorkerAllocationService, Mockito.times(2)).getAllocationsForKeyworker(anyLong());

        // Verify returned KW is the one with fewest allocations and least recent auto-allocation
        assertThat(allocatedKeyworker.getStaffId()).isEqualTo(staffId3);
    }

    // Given an offender is seeking KW allocation
    // And there are multiple KWs in the KWP
    // And offender has been previously allocated to one of the KWs in KWP
    // When KWP requested for KW for offender
    // Then KW that offender was previously allocated to is returned
    //
    // If this test fails, an offender will not be allocated to a Key worker they have previously been allocated to.
    @Test
    public void testPreviouslyAllocatedKeyworkerIsReturned() {
        // Multiple KWs, all with capacity, in KWP
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long allocBookingId = 1;
        final long allocStaffId = 2;

        List<Keyworker> keyworkers = getKeyworkers(3, lowAllocCount, highAllocCount);
        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, keyworkers, capacityTiers);

        // A previous allocation between the unallocated offender and Key worker with staffId = 2
        mockPrisonerAllocationHistory(keyWorkerAllocationService,
                getPreviousKeyworkerAllocation(allocBookingId, allocStaffId));

        // Request KW from pool for offender
        Keyworker allocatedKeyworker = keyworkerPool.getKeyworker(allocBookingId);

        // Verify that returned KW is one to whom offender was previously allocated
        assertThat(allocatedKeyworker.getStaffId()).isEqualTo(allocStaffId);
    }

    // Given an offender is seeking KW allocation
    // And there are multiple KWs in the KWP
    // And offender has been previously allocated, at different times, to several of the KWs in KWP
    // When KWP requested for KW for offender
    // Then KW that offender was most recently previously allocated to is returned
    //
    // If this test fails, an offender will not be allocated to the Key worker they were most recently allocated to.
    @Test
    public void testMostRecentPreviouslyAllocatedKeyworkerIsReturned() {
        // Multiple KWs, all with capacity, in KWP
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long allocBookingId = 1;
        final long allocStaffIdMostRecent = 4;
        final long allocStaffIdOther = 3;
        final long allocStaffIdLeastRecent = 2;
        final LocalDateTime ldtMostRecent = LocalDateTime.now().minusDays(7);
        final LocalDateTime ldtOther = ldtMostRecent.minusDays(7);
        final LocalDateTime ldtLeastRecent = ldtOther.minusDays(7);

        List<Keyworker> keyworkers = getKeyworkers(7, lowAllocCount, highAllocCount);
        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, keyworkers, capacityTiers);

        // Previous allocations between the unallocated offender and previous KWs
        mockPrisonerAllocationHistory(keyWorkerAllocationService,
                getPreviousKeyworkerAllocation(allocBookingId, allocStaffIdMostRecent, ldtMostRecent),
                getPreviousKeyworkerAllocation(allocBookingId, allocStaffIdOther, ldtOther),
                getPreviousKeyworkerAllocation(allocBookingId, allocStaffIdLeastRecent, ldtLeastRecent));

        // Request KW from pool for offender
        Keyworker allocatedKeyworker = keyworkerPool.getKeyworker(allocBookingId);

        // Verify that returned KW is one to whom offender was most recently previously allocated
        assertThat(allocatedKeyworker.getStaffId()).isEqualTo(allocStaffIdMostRecent);
    }

    // Given a KW is not in the KWP
    // When attempt is made to refresh KW in the KWP
    // Then an IllegalStateException is thrown because the KW is not in the KWP
    //
    // If this test fails, a KW who is not a member of the KWP may be added to the KWP when they shouldn't be
    @Test(expected = IllegalStateException.class)
    public void testExceptionThrownWhenKeyworkerRefreshedButNotMemberOfKeyworkerPool() {
        // KWP initialised with an initial set of KWs
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;

        List<Keyworker> keyworkers = getKeyworkers(7, lowAllocCount, highAllocCount);
        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, keyworkers, capacityTiers);

        // A KW who is not a member of KWP
        Keyworker otherKeyworker = getKeyworker(8, 5);

        // Attempt refresh
        keyworkerPool.refreshKeyworker(otherKeyworker);
    }

    // Given a KW is in the KWP
    // When attempt is made to refresh KW in the KWP
    // Then attempt is successful and KWP is updated with refreshed KW
    //
    // If this test fails, a KW who is a member of the KWP may not be refreshed correctly in KWP and this may result in
    // incorrect allocations taking place for the KW due to KWP having an out-of-date KW entry.
    @Test
    public void testKeyworkerRefreshedWhentMemberOfKeyworkerPool() {
        // KWP initialised with an initial set of KWs
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        final long refreshKeyworkerStaffId = 27;

        List<Keyworker> keyworkers = getKeyworkers(5, lowAllocCount, highAllocCount);

        // Add another couple of KWs with known allocation counts (one high, one low)
        Keyworker lowAllocKeyworker = getKeyworker(refreshKeyworkerStaffId - 1, lowAllocCount);
        Keyworker highAllocKeyworker = getKeyworker(refreshKeyworkerStaffId, highAllocCount);

        keyworkers.add(lowAllocKeyworker);
        keyworkers.add(highAllocKeyworker);

        keyworkerPool = initKeyworkerPool(keyWorkerAllocationService, keyworkers, capacityTiers);

        // Verify that priority KW is not the one with known high alloc count
        Keyworker priorityKeyworker = keyworkerPool.getKeyworker(1);

        assertThat(priorityKeyworker).isNotSameAs(highAllocKeyworker);

        // Simulate refreshed high alloc KW (now having zero allocations).
        Keyworker refreshedHighAllocKeyworker = getKeyworker(refreshKeyworkerStaffId, 0);

        // Attempt refresh
        keyworkerPool.refreshKeyworker(refreshedHighAllocKeyworker);

        // Verify that priority KW is our refreshed high-alloc Key worker (that now has zero allocations)
        priorityKeyworker = keyworkerPool.getKeyworker(2);

        assertThat(priorityKeyworker).isSameAs(refreshedHighAllocKeyworker);
    }
}