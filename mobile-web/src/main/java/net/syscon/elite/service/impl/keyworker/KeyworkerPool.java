package net.syscon.elite.service.impl.keyworker;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;
import org.apache.commons.lang3.Validate;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a collection of Key workers that are available for allocation and encapsulates the implementation of
 * allocation rules which govern which Key worker is next in line for allocation at any particular moment.
 *
 * NB: KeyworkerPool is not thread safe. The pool is designed for short-lived, single-threaded operation. As a result
 * an instance of KeyworkerPool will not support multiple auto-allocation processes. Each auto-allocation process
 * should instantiate and use its own KeyworkerPool.
 */
@Slf4j
public class KeyworkerPool {
    public static final String OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY = "All available Key workers are at full capacity.";

    private final SortedSet<Keyworker> keyworkerPool;
    private final Map<Long,List<KeyWorkerAllocation>> keyworkerAllocations;
    private final SortedSet<Integer> capacityTiers;
    private final Set<Long> keyworkerStaffIds;
    private final Integer maxCapacity;

    private KeyWorkerAllocationService keyWorkerAllocationService;

    /**
     * Constructor.
     *
     * @param keyworkers set of Key workers in the pool.
     * @param capacityTiers optional set of capacity tier levels.
     */
    KeyworkerPool(Collection<Keyworker> keyworkers, Collection<Integer> capacityTiers) {
        Validate.notEmpty(keyworkers, "Key worker pool must contain at least one Key worker.");

        // Initialise capacity tiers
        if (ObjectUtils.isEmpty(capacityTiers)) {
            this.capacityTiers = new TreeSet<>(Collections.singleton(Integer.MAX_VALUE));
        } else {
            this.capacityTiers = new TreeSet<>(capacityTiers);
        }

        maxCapacity = this.capacityTiers.last();

        // Initialise key worker pool
        keyworkerStaffIds = new HashSet<>();
        keyworkerAllocations = new HashMap<>();

        keyworkers.forEach(kw -> {
            keyworkerStaffIds.add(kw.getStaffId());
            keyworkerAllocations.put(kw.getStaffId(), null);
        });

        keyworkerPool = new TreeSet<>(buildKeyworkerComparator());
        keyworkerPool.addAll(keyworkers);

        log.debug("Key worker pool initialised with {} members.", keyworkers.size());
    }

    public void setKeyWorkerAllocationService(KeyWorkerAllocationService keyWorkerAllocationService) {
        this.keyWorkerAllocationService = keyWorkerAllocationService;
    }

    // Constructs Key worker comparator which, effectively, implements Key worker allocation prioritisation algorithm.
    // Comparator function ensures that highest priority Key worker is at head of Key worker pool.
    private Comparator<Keyworker> buildKeyworkerComparator() {
        return Comparator.comparingInt(Keyworker::getNumberAllocated)
                .thenComparing(Keyworker::getStaffId, (id1, id2) -> {
                    Comparator<KeyWorkerAllocation> keyWorkerAllocationComparator =
                            Comparator.comparing(KeyWorkerAllocation::getAssigned);

                    SortedSet<KeyWorkerAllocation> id1Allocations = new TreeSet<>(keyWorkerAllocationComparator);
                    SortedSet<KeyWorkerAllocation> id2Allocations = new TreeSet<>(keyWorkerAllocationComparator);

                    Optional.ofNullable(keyworkerAllocations.get(id1)).ifPresent(id1Allocations::addAll);
                    Optional.ofNullable(keyworkerAllocations.get(id2)).ifPresent(id2Allocations::addAll);

                    int result;

                    // If neither Key worker has any auto-allocations, or both have auto-allocations and an identical
                    // assigned datetime for most recent allocation, arbitrarily sort by staffId (to ensure uniqueness).
                    if (id1Allocations.isEmpty()) {
                        result = id2Allocations.isEmpty() ? (id1.compareTo(id2)) : -1;
                    } else if (id2Allocations.isEmpty()) {
                        result = 1;
                    } else {
                        result = id1Allocations.first().getAssigned().compareTo(id2Allocations.first().getAssigned());

                        if (result == 0) {
                            result = id1.compareTo(id2);
                        }
                    }

                    return result;
        });
    }

    /**
     * Identifies and returns Key worker to whom offender should be allocated, as determined by allocation rules. Note
     * that if the returned Key worker is used for allocation of an offender, the pool must be updated with refreshed
     * Key worker details (via {@link #refreshKeyworker(Keyworker)}).
     *
     * @param bookingId booking id of offender for whom key worker allocation is required.
     * @return the priority {@code Keyworker}.
     */
    public Keyworker getKeyworker(long bookingId) {
        log.debug("Prioritising Key worker for allocation of offender with bookingId [{}]", bookingId);

        // Retrieve any previous Key worker allocations for offender.
        List<KeyWorkerAllocation> previousAllocations =
                keyWorkerAllocationService.getAllocationHistoryForPrisoner(bookingId, null, null);

        // First, determine if offender was previously allocated to any Key workers in the pool
        Optional<Keyworker> previousKeyworker = findPreviousAllocation(bookingId, previousAllocations);

        Keyworker priorityKeyworker;

        if (previousKeyworker.isPresent()) {
            priorityKeyworker = previousKeyworker.get();

            log.debug("Previous allocation detected between Key worker with staffId [{}] and offender with bookingId [{}].",
                    priorityKeyworker.getStaffId(), bookingId);
        } else {
            log.debug("No previous Key worker allocations detected for offender with bookingId [{}].", bookingId);

            prioritiseKeyworkers();

            // Check allocation level for Key worker at head of pool list - if at or beyond maximum capacity, then
            // error as no Key worker currently in pool can accept any further allocations.
            checkMaxCapacity();

            priorityKeyworker = keyworkerPool.first();
        }

        log.debug("Key worker with staffId [{}] selected for allocation of offender with bookingId [{}].",
                priorityKeyworker.getStaffId(), bookingId);

        return priorityKeyworker;
    }

    /**
     * Refreshes pool with provided Key worker. If provided Key worker does not already exist in the pool, an
     * {@code IllegalStateException} is throwm.
     *
     * @param keyworker Key worker to refresh.
     * @throws IllegalStateException if Key worker is not already present in the pool.
     */
    public void refreshKeyworker(Keyworker keyworker) {
        Validate.notNull(keyworker, "Key worker to refresh must be specified.");

        // Remove Key worker from pool (throwing exception if Key worker not in pool)
        if (!removeKeyworker(keyworker.getStaffId()).isPresent()) {
            log.error("Key worker with staffId [{}] not in pool.", keyworker.getStaffId());

            throw new IllegalStateException("Key worker to refresh is not in Key worker pool.");
        }

        // Add Key worker back to pool
        reinstateKeyworker(keyworker, null);
    }

    private Optional<Keyworker> findPreviousAllocation(long bookingId, List<KeyWorkerAllocation> keyWorkerAllocations) {
        log.debug("Assessing previous allocations for offender with bookingId [{}].", bookingId);

        Optional<Keyworker> previousKeyworker;

        // Check if any were with a Key worker in the pool and sort allocations by assigned date
        if (ObjectUtils.isEmpty(keyWorkerAllocations)) {
            previousKeyworker = Optional.empty();
        } else {
            Optional<KeyWorkerAllocation> latestAllocation = keyWorkerAllocations.stream()
                    .filter(kwa -> kwa.getBookingId().equals(bookingId) && keyworkerStaffIds.contains(kwa.getStaffId()))
                    .max(Comparator.comparing(KeyWorkerAllocation::getAssigned));

            if (latestAllocation.isPresent()) {
                // Key worker staff id of latest allocation
                Long keyworkerStaffId = latestAllocation.get().getStaffId();

                previousKeyworker = keyworkerPool.stream().filter(kw -> keyworkerStaffId.equals(kw.getStaffId())).findFirst();
            } else {
                previousKeyworker = Optional.empty();
            }
        }

        return previousKeyworker;
    }

    // Applies sorting algorithm to Key worker list to determine allocation priority of Key workers. Must be called
    // prior to each request for priority Key worker.
    private void prioritiseKeyworkers() {
        checkMaxCapacity();

        // Identify Key worker(s) with least number of allocations - first Key worker in pool will have least allocations
        int fewestAllocs = keyworkerPool.first().getNumberAllocated();

        // If priority Key worker hos no allocations, no further processing required, otherwise identify any other Key
        // workers in pool having same number of allocations.
        if (fewestAllocs > 0) {
            List<Keyworker> fewestAllocKeyworkers =
                    keyworkerPool.stream().filter(kw -> (kw.getNumberAllocated() == fewestAllocs)).collect(Collectors.toList());

            // If only one Key worker with fewest allocations, no further processing required, otherwise retrieve
            // allocations for all Key workers with fewest allocations and update sort.
            if (fewestAllocKeyworkers.size() > 1) {
                // For each Key worker with fewest allocations, remove from pool, update allocations, reinstate to pool.
                fewestAllocKeyworkers.forEach(kw -> {
                    removeKeyworker(kw.getStaffId());

                    Long staffId = kw.getStaffId();
                    List<KeyWorkerAllocation> allocations = keyWorkerAllocationService.getAllocationsForKeyworker(staffId);

                    reinstateKeyworker(kw, allocations);
                });
            }
        }

        log.debug("Key worker pool prioritised - priority Key worker has {} allocations.",
                keyworkerPool.first().getNumberAllocated());
    }

    private void checkMaxCapacity() {
        if (keyworkerPool.first().getNumberAllocated() >= maxCapacity) {
            log.error(OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);

            throw AllocationException.withMessage(OUTCOME_ALL_KEY_WORKERS_AT_CAPACITY);
        }
    }

    private Optional<Keyworker> removeKeyworker(long staffId) {
        log.debug("Removing Key worker with staffId [{}] from pool.", staffId);

        keyworkerStaffIds.remove(staffId);

        Optional<Keyworker> keyworker = keyworkerPool.stream().filter(kw -> kw.getStaffId().equals(staffId)).findFirst();

        keyworker.ifPresent(keyworkerPool::remove);

        keyworkerAllocations.remove(staffId);

        return keyworker;
    }

    private void reinstateKeyworker(Keyworker keyworker, List<KeyWorkerAllocation> allocations) {
        Long staffId = keyworker.getStaffId();

        log.debug("Reinstating Key worker with staffIf [{}], and having [{}] allocations, to pool.",
                staffId, keyworker.getNumberAllocated());

        // Filter out manual allocations (only interested in auto-allocations)
        List<KeyWorkerAllocation> autoAllocations = (allocations == null) ? null :
                allocations.stream().filter(kwa -> !kwa.isManualAllocation()).collect(Collectors.toList());

        keyworkerAllocations.put(staffId, autoAllocations);
        keyworkerPool.add(keyworker);
        keyworkerStaffIds.add(staffId);

        log.debug("Key worker with staffId [{}] reinstated to pool. New pool size is [{}] and priority Key worker has staffId [{}] and [{}] allocations.",
                staffId, keyworkerPool.size(), keyworkerPool.first().getStaffId(), keyworkerPool.first().getNumberAllocated());
    }
}
