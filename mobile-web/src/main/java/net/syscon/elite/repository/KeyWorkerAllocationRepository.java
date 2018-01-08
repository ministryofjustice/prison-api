package net.syscon.elite.repository;

import net.syscon.elite.repository.impl.KeyWorkerAllocation;

import java.util.List;

/**
 * Key Worker Allocation API repository interface.
 */
public interface KeyWorkerAllocationRepository {
    void createAllocation(KeyWorkerAllocation allocation, String username);

    void deactivateCurrentAllocationForOffenderBooking(Long bookingId, String username);

    KeyWorkerAllocation getCurrentAllocationForOffenderBooking(Long bookingId);

    List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId);
}
