package net.syscon.elite.service.impl.keyworker;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.keyworker.AllocationService;
import org.apache.commons.lang3.Validate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a collection of Key workers that are available for allocation and encapsulates the implementation of
 * allocation rules which govern which Key worker is next in line for allocation at any particular moment.
 */
@Slf4j
public class KeyworkerPool {
    private final List<Keyworker> keyworkers;
    private final List<Integer> capacityTiers;
    private final Integer maxCapacity;

    private final Comparator<Keyworker> keyworkerComparator;

    /**
     * Constructor.
     *
     * @param keyworkers list of Key workers in the pool.
     * @param capacityTiers optional list of capacity tier levels.
     */
    public KeyworkerPool(List<Keyworker> keyworkers, List<Integer> capacityTiers) {
        Validate.notEmpty(keyworkers, "Key worker pool must contain at least one Key worker.");

        this.keyworkers = keyworkers;

        if (Objects.isNull(capacityTiers)) {
            maxCapacity = Integer.MAX_VALUE;
            this.capacityTiers = Collections.singletonList(maxCapacity);
        } else {
            OptionalInt maxCap = capacityTiers.stream().mapToInt(Integer::intValue).max();

            if (maxCap.isPresent()) {
                maxCapacity = maxCap.getAsInt();
                this.capacityTiers = capacityTiers.stream().sorted().collect(Collectors.toList());
            } else {
                maxCapacity = Integer.MAX_VALUE;
                this.capacityTiers = Collections.singletonList(maxCapacity);
            }
        }

        log.debug("Key worker pool initialised with {} members.", keyworkers.size());

        keyworkerComparator = buildKeyworkerComparator();
    }

    // Constructs Key worker comparator which, effectively, implements Key worker allocation prioritisation algorithm.
    // Comparator function ensures that highest priority Key worker is at head of Key worker pool.
    private Comparator<Keyworker> buildKeyworkerComparator() {
        return Comparator.comparing(Keyworker::getNumberAllocated);
    }

    /**
     * Gets number of Key workers in pool.
     *
     * @return count of Key workers in pool.
     */
    public int getPoolSize() {
        return keyworkers.size();
    }

    /**
     * Gets the <i>normal</i> maximum number of allocations that a Key worker can have. In certain circumstances, a
     * Key worker's allocations may exceed the maximum.
     *
     * @return maximum allocation count for a Key worker.
     */
    public int getMaxAllocations() {
        return maxCapacity;
    }

    /**
     * Returns Key worker who currently has priority for allocation, as determined by allocation rules.
     *
     * @return the priority {@code Keyworker}.
     */
    public Keyworker getPriorityKeyworker() {
        prioritiseKeyworkers();

        // Check allocation level for Key worker at head of pool list - if at or beyond maximum capacity, then error as
        // no Key worker currently in pool can accept any further allocations.
        Keyworker priorityKeyworker = keyworkers.get(0);

        if (priorityKeyworker.getNumberAllocated() >= maxCapacity) {
            log.error(AllocationService.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);

            throw AllocationException.withMessage(AllocationService.OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
        }

        log.debug("Key worker pool prioritised - priority Key worker has {} allocations.",
                priorityKeyworker.getNumberAllocated());

        return priorityKeyworker;
    }

    // Applies sorting algorithm to Key worker list to determine allocation priority of Key workers. Must be called
    // prior to each request for priority Key worker.
    private void prioritiseKeyworkers() {
        keyworkers.sort(keyworkerComparator);
    }
}
