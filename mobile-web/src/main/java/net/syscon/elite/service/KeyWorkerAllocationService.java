package net.syscon.elite.service;

import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;

import java.util.List;

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
}
