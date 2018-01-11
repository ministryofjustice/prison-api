package net.syscon.elite.repository;

import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;

import java.util.List;
import java.util.Optional;

/**
 * Key Worker Allocation API repository interface.
 */
public interface KeyWorkerAllocationRepository {
    void createAllocation(KeyWorkerAllocation allocation, String username);

    void deactivateAllocationForOffenderBooking(Long bookingId, String reason, String username);

    Optional<KeyWorkerAllocation> getCurrentAllocationForOffenderBooking(Long bookingId);

    List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId, String orderByFields, Order order);

    Optional<KeyWorkerAllocation> getLatestAllocationForOffenderBooking(Long bookingId);

    void deactivateAllocationsForKeyWorker(Long staffId, String reason, String username);
}
