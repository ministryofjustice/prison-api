package net.syscon.elite.service;

import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;

import java.util.List;
import java.util.Set;

/**
 * Key Worker Allocation service interface.
 */
public interface KeyWorkerAllocationService {

    void createAllocation(KeyWorkerAllocation allocation, String username);

    void deactivateAllocationForKeyWorker(Long staffId, String reason, String username);

    void deactivateAllocationForOffenderBooking(Long bookingId, String reason, String username);

    List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId, String orderByFields, Order order);

    KeyWorkerAllocation getCurrentAllocationForOffenderBooking(Long bookingId);

    KeyWorkerAllocation getLatestAllocationForOffenderBooking(Long bookingId);

    Page<OffenderSummary> getUnallocatedOffenders(Set<String> agencyFilter, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder);

    List<Keyworker> getAvailableKeyworkers(String agencyId);
}
