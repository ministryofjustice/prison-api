package net.syscon.elite.service.impl.keyworker;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import net.syscon.elite.service.keyworker.AllocationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@Slf4j
public class AutoAllocationServiceImpl implements AllocationService {
    private KeyWorkerAllocationService keyWorkerAllocationService;
    private KeyworkerPoolFactory keyworkerPoolFactory;

    /**
     * Constructor.
     *
     * @param keyWorkerAllocationService key worker allocation service.
     * @param keyworkerPoolFactory factory that facilitates creation of Key worker pools.
     */
    public AutoAllocationServiceImpl(KeyWorkerAllocationService keyWorkerAllocationService,
                                     KeyworkerPoolFactory keyworkerPoolFactory) {
        this.keyWorkerAllocationService = keyWorkerAllocationService;
        this.keyworkerPoolFactory = keyworkerPoolFactory;
    }

    @Override
    public void autoAllocate(String agencyId) throws AllocationException {
        // Confirm a valid agency has been supplied.
        Validate.isTrue(!StringUtils.isBlank(agencyId), "Agency id must be provided.");

        // Get initial page of unallocated offenders for agency
        Page<OffenderSummary> unallocatedOffenders = getFirstPageUnallocatedOffenders(agencyId, 10L);

        // Are there any unallocated offenders? If not, log and exit, otherwise proceed.
        if (unallocatedOffenders.getItems().isEmpty()) {
            log.info(OUTCOME_NO_UNALLOCATED_OFFENDERS);
        } else {
            List<Keyworker> availableKeyworkers = keyWorkerAllocationService.getAvailableKeyworkers(agencyId);

            if (availableKeyworkers.isEmpty()) {
                log.error(OUTCOME_NO_AVAILABLE_KEY_WORKERS);

                throw AllocationException.withMessage(OUTCOME_NO_AVAILABLE_KEY_WORKERS);
            }

            // At this point, we have some unallocated offenders and some available Key workers. Let's put the Key
            // workers into a pool then start processing allocations.
            KeyworkerPool keyworkerPool = keyworkerPoolFactory.getKeyworkerPool(availableKeyworkers);
            processAllocations(unallocatedOffenders, keyworkerPool);
        }
    }

    private void processAllocations(Page<OffenderSummary> offenders, KeyworkerPool keyworkerPool) {
        log.debug("Processing allocation for {} unallocated offenders to {} available Key workers.",
                offenders.getItems().size(), keyworkerPool.getPoolSize());

        // Process allocation for each unallocated offender
        for (OffenderSummary offender : offenders.getItems()) {
            processAllocation(offender, keyworkerPool);
        }
    }

    private void processAllocation(OffenderSummary offender, KeyworkerPool keyworkerPool) {
        // Retrieve previous allocations for offender
        List<KeyWorkerAllocation> keyWorkerAllocations = getKeyWorkerAllocations(offender.getBookingId());

        // If there are previous allocations, check if any were with a currently available Key worker. If no
        // previous allocations, allocate to one of the available Key workers according to allocation rules.
        if (keyWorkerAllocations.isEmpty()) {
            // Get priority Key worker from pool
            Keyworker keyworker = keyworkerPool.getPriorityKeyworker();
        } else {
            // Check if offender was previously allocated to one of the currently available Key workers
        }
    }

    private Page<OffenderSummary> getFirstPageUnallocatedOffenders(String agencyId, long pageLimit) {
        return keyWorkerAllocationService.getUnallocatedOffenders(
                agencyId, 0L, pageLimit, null, null);
    }

    private List<KeyWorkerAllocation> getKeyWorkerAllocations(long bookingId) {
        return keyWorkerAllocationService.getAllocationHistoryForPrisoner(bookingId, null, null);
    }
}
