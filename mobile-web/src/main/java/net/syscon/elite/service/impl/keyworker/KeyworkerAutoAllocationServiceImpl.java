package net.syscon.elite.service.impl.keyworker;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import net.syscon.elite.service.keyworker.KeyworkerAutoAllocationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.boot.actuate.metrics.buffer.BufferMetricReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation of Key worker auto-allocation. On initiation the auto-allocation process will attempt to
 * allocate all unallocated offenders within specified agency. If there is insufficient capacity among available Key
 * workers to accommodate allocation of all unallocated offenders, offenders will be allocated until there is no
 * further capacity and then the auto-allocation process will terminate.
 */
@Service
@Transactional(noRollbackFor = {AllocationException.class})
@Slf4j
public class KeyworkerAutoAllocationServiceImpl implements KeyworkerAutoAllocationService {
    public static final String COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS = "counter.keyworker.allocations.auto";
    public static final String OUTCOME_NO_UNALLOCATED_OFFENDERS = "No unallocated offenders.";
    public static final String OUTCOME_NO_AVAILABLE_KEY_WORKERS = "No Key workers available for allocation.";
    public static final String OUTCOME_AUTO_ALLOCATION_SUCCESS = "Offender with bookingId [{}] successfully auto-allocated to Key worker.";

    private final KeyWorkerAllocationService keyWorkerAllocationService;
    private final KeyworkerPoolFactory keyworkerPoolFactory;
    private final CounterService counterService;
    private final BufferMetricReader metricReader;
    private final long offenderPageLimit;

    /**
     * Constructor.
     *
     * @param keyWorkerAllocationService key worker allocation service.
     * @param keyworkerPoolFactory factory that facilitates creation of Key worker pools.
     */
    public KeyworkerAutoAllocationServiceImpl(KeyWorkerAllocationService keyWorkerAllocationService,
                                              KeyworkerPoolFactory keyworkerPoolFactory,
                                              CounterService counterService,
                                              BufferMetricReader metricReader) {
        this.keyWorkerAllocationService = keyWorkerAllocationService;
        this.keyworkerPoolFactory = keyworkerPoolFactory;
        this.counterService = counterService;
        this.metricReader = metricReader;

        this.offenderPageLimit = 10L;
    }

    @Override
    @VerifyAgencyAccess
    public Long autoAllocate(String agencyId) throws AllocationException {
        // Confirm a valid agency has been supplied.
        Validate.isTrue(StringUtils.isNotBlank(agencyId), "Agency id must be provided.");

        log.info("Key worker auto-allocation process initiated for agency [{}].", agencyId);

        // Get initial counter metric
        long startAllocCount = getCurrentAllocationCount();

        // Get initial page of unallocated offenders for agency
        Page<OffenderSummary> unallocatedOffenders = getPageUnallocatedOffenders(agencyId, offenderPageLimit);

        // Are there any unallocated offenders? If not, log and exit, otherwise proceed.
        if (unallocatedOffenders.getItems().isEmpty()) {
            log.info(OUTCOME_NO_UNALLOCATED_OFFENDERS);
        } else {
            List<Keyworker> availableKeyworkers = keyWorkerAllocationService.getAvailableKeyworkers(agencyId);

            if (availableKeyworkers.isEmpty()) {
                log.error(OUTCOME_NO_AVAILABLE_KEY_WORKERS);

                throw AllocationException.withMessage(OUTCOME_NO_AVAILABLE_KEY_WORKERS);
            }

            log.info("Proceeding with auto-allocation for {} unallocated offenders and {} available Key workers at agency [{}].",
                    unallocatedOffenders.getTotalRecords(), availableKeyworkers.size(), agencyId);

            // At this point, we have some unallocated offenders and some available Key workers. Let's put the Key
            // workers into a pool then start processing allocations.
            KeyworkerPool keyworkerPool = keyworkerPoolFactory.getKeyworkerPool(availableKeyworkers);

            // Continue processing allocations for unallocated offenders until no further unallocated offenders exist
            // or Key workers no longer have capacity.
            try {
                while (!unallocatedOffenders.getItems().isEmpty()) {
                    processAllocations(unallocatedOffenders, keyworkerPool);

                    unallocatedOffenders = getPageUnallocatedOffenders(agencyId, offenderPageLimit);
                }
            } catch(AllocationException aex) {
                long allocCount = calcAndLogAllocationsProcessed(agencyId, startAllocCount);

                log.info("Key worker auto-allocation terminated after processing {} allocations.", allocCount);
                log.error("Reason for termination: {}", aex.getMessage());

                throw aex;
            }
        }

        return calcAndLogAllocationsProcessed(agencyId, startAllocCount);
    }

    private void processAllocations(Page<OffenderSummary> offenderPage, KeyworkerPool keyworkerPool) {
        // Process allocation for each unallocated offender
        for (OffenderSummary offender : offenderPage.getItems()) {
            processAllocation(offender.getBookingId(), keyworkerPool);
        }
    }

    private void processAllocation(long bookingId, KeyworkerPool keyworkerPool) {
        Keyworker keyworker = keyworkerPool.getKeyworker(bookingId);

        // At this point, Key worker to which offender will be allocated has been identified - create allocation
        confirmAllocation(bookingId, keyworker);

        // Update Key worker pool with refreshed Key worker (following successful allocation)
        Keyworker refreshedKeyworker = keyWorkerAllocationService.getKeyworkerDetails(keyworker.getStaffId());

        keyworkerPool.refreshKeyworker(refreshedKeyworker);
    }

    private Page<OffenderSummary> getPageUnallocatedOffenders(String agencyId, long pageLimit) {
        return keyWorkerAllocationService.getUnallocatedOffenders(
                agencyId, 0L, pageLimit, null, null);
    }

    private void confirmAllocation(long bookingId, Keyworker keyworker) {
        NewAllocation newAllocation = buildNewAutoAllocation(bookingId, keyworker);

        keyWorkerAllocationService.allocate(newAllocation);

        counterService.increment(COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS);

        log.info(OUTCOME_AUTO_ALLOCATION_SUCCESS, bookingId);
    }

    private NewAllocation buildNewAutoAllocation(long bookingId, Keyworker keyworker) {
        return NewAllocation.builder()
                .bookingId(bookingId)
                .staffId(keyworker.getStaffId())
                .type(AllocationType.AUTO.getIndicator())
                .reason(KeyworkerAutoAllocationService.ALLOCATION_REASON_AUTO)
                .build();
    }

    private Long calcAndLogAllocationsProcessed(String agencyId, long startAllocCount) {
        // Determine total allocations for this execution of auto-allocation process.
        long allocCount = getCurrentAllocationCount() - startAllocCount;

        log.info("Processed {} allocations for agency [{}].", allocCount, agencyId);

        return allocCount;
    }

    private long getCurrentAllocationCount() {
        long allocCount = 0;

        Metric metricAllocCount = metricReader.findOne(COUNTER_METRIC_KEYWORKER_AUTO_ALLOCATIONS);

        if (metricAllocCount != null) {
            allocCount = metricAllocCount.getValue().longValue();
        }

        return allocCount;
    }
}