package net.syscon.elite.service.impl.keyworker;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Page;
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
    public static final String OUTCOME_NO_UNALLOCATED_OFFENDERS = "No unallocated offenders.";
    public static final String OUTCOME_NO_AVAILABLE_KEY_WORKERS = "No Key workers available for allocation.";
    public static final String OUTCOME_AUTO_ALLOCATION_SUCCESS = "Offender with bookingId [{}] successfully auto-allocated to Key worker.";

    private final KeyWorkerAllocationService keyWorkerAllocationService;
    private final KeyworkerPoolFactory keyworkerPoolFactory;
    private final long offenderPageLimit;

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

        this.offenderPageLimit = 10L;
    }

    @Override
    public void autoAllocate(String agencyId) throws AllocationException {
        // Confirm a valid agency has been supplied.
        Validate.isTrue(StringUtils.isNotBlank(agencyId), "Agency id must be provided.");

        // Get initial page of unallocated offenders for agency
        Page<OffenderSummary> unallocatedOffenders = getPageUnallocatedOffenders(agencyId, 0L, offenderPageLimit);

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

            processAllocations(agencyId, unallocatedOffenders, keyworkerPool);
        }
    }

    private void processAllocations(String agencyId, Page<OffenderSummary> offenderPage, KeyworkerPool keyworkerPool) {
        log.debug("Processing allocations for {} unallocated offenders to {} available Key workers.",
                offenderPage.getTotalRecords(), keyworkerPool.getPoolSize());

        // Process allocation for each unallocated offender
        for (OffenderSummary offender : offenderPage.getItems()) {
            processAllocation(offender.getBookingId(), keyworkerPool);
        }

        // Process allocations for next page of offenders, if required
        long nextPageOffset = offenderPage.getPageOffset() + offenderPageLimit;

        if (nextPageOffset < offenderPage.getTotalRecords()) {
            Page<OffenderSummary> nextPage = getPageUnallocatedOffenders(agencyId, nextPageOffset, offenderPageLimit);

            processAllocations(agencyId, nextPage, keyworkerPool);
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

    private Page<OffenderSummary> getPageUnallocatedOffenders(String agencyId, long pageOffset, long pageLimit) {
        return keyWorkerAllocationService.getUnallocatedOffenders(
                agencyId, pageOffset, pageLimit, null, null);
    }

    private void confirmAllocation(long bookingId, Keyworker keyworker) {
        NewAllocation newAllocation = buildNewAutoAllocation(bookingId, keyworker);

        keyWorkerAllocationService.allocate(newAllocation);

        log.info(OUTCOME_AUTO_ALLOCATION_SUCCESS, bookingId);
    }

    private NewAllocation buildNewAutoAllocation(long bookingId, Keyworker keyworker) {
        return NewAllocation.builder()
                .bookingId(bookingId)
                .staffId(keyworker.getStaffId())
                .type(AllocationType.AUTO.getIndicator())
                .reason(AllocationService.ALLOCATION_REASON_AUTO)
                .build();
    }
}
