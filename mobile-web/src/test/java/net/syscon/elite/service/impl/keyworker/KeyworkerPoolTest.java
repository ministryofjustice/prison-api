package net.syscon.elite.service.impl.keyworker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.Appender;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.keyworker.AllocationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Unit test for Key worker pool.
 */
@RunWith(MockitoJUnitRunner.class)
public class KeyworkerPoolTest {
    private static final int CAPACITY_TIER_1 = 6;
    private static final int CAPACITY_TIER_2 = 9;
    private static final int FULLY_ALLOCATED = CAPACITY_TIER_2;

    private KeyworkerPool keyworkerPool;
    private List<Integer> capacityTiers;

    @Mock
    private Appender mockAppender;

    @Before
    public void setup() {
        // Initialise Key worker allocation capacity tiers
        capacityTiers = new ArrayList<>();

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

    // Given there is a single KW in the KWP
    // And that KW has capacity
    // When KWP requested for priority KW
    // Then single KW in KWP is returned
    @Test
    public void testSingleKeyworkerWithCapacityHasPriority() {
        // Single KW, with capacity, in KWP
        Keyworker keyworker = KeyworkerTestHelper.getKeyworker(1, CAPACITY_TIER_1);
        keyworkerPool = new KeyworkerPool(Collections.singletonList(keyworker), capacityTiers);

        // Request priority KW from pool
        Keyworker priorityKeyworker = keyworkerPool.getPriorityKeyworker();

        // Verify same KW used to initialise pool is returned
        assertThat(priorityKeyworker).isSameAs(keyworker);
    }

    // Given there is a single KW in the KWP
    // And that KW is fully allocated (has no capacity)
    // When KWP requested for priority KW
    // Then an error is logged with message to effect that all Key workers are fully allocated
    // And an AllocationException is thrown with message to effect that all Key workers are fully allocated
    @Test
    public void testPoolErrorsWhenSingleKeyworkerIsFullyAllocated() {
        // Single KW, fully allocated, in KWP
        Keyworker keyworker = KeyworkerTestHelper.getKeyworker(1, FULLY_ALLOCATED);
        keyworkerPool = new KeyworkerPool(Collections.singletonList(keyworker), capacityTiers);

        // Request priority KW from pool (catching expected exception)
        Throwable thrown = catchThrowable(() -> keyworkerPool.getPriorityKeyworker());

        // Verify log output and exception
        KeyworkerTestHelper.verifyLog(mockAppender, Level.ERROR, AllocationService.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);

        assertThat(thrown)
                .isInstanceOf(AllocationException.class)
                .hasMessage(AllocationService.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
    }

    // Given there are multiple KWs in the KWP
    // And all KWs have differing capacity (but none are fully allocated)
    // When KWP requested for priority KW
    // Then KW with most capacity is returned
    @Test
    public void testKeyworkerWithMostCapacityHasPriority() {
        // Multiple KWs, all with capacity, in KWP
        final int lowAllocCount = 1;
        final int highAllocCount = FULLY_ALLOCATED - 1;
        List<Keyworker> keyworkers = KeyworkerTestHelper.getKeyworkers(3, lowAllocCount, highAllocCount);
        keyworkerPool = new KeyworkerPool(keyworkers, capacityTiers);

        // Request priority KW from pool
        Keyworker priorityKeyworker = keyworkerPool.getPriorityKeyworker();

        // Verify returned KW is the one with fewest allocations
        OptionalInt fewestAllocs = keyworkers.stream().mapToInt(Keyworker::getNumberAllocated).min();
        assertThat(fewestAllocs.isPresent()).isTrue();
        assertThat(priorityKeyworker.getNumberAllocated()).isEqualTo(fewestAllocs.getAsInt());
    }
}
