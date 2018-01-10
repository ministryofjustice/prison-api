package net.syscon.elite.service;

import net.syscon.elite.repository.impl.KeyWorkerAllocation;

import java.util.List;

/**
 * Key Worker Allocation service interface.
 */
public interface KeyWorkerAllocationService {

    void createAllocation(KeyWorkerAllocation allocation, String username);

    void deactivateAllocationForKeyWorker(Long staffId, String username);

    void deactivateAllocationForOffenderBooking(Long bookingId, String username);

    List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long offenderId);

    KeyWorkerAllocation getCurrentAllocationForOffenderBooking(Long bookingId);

    KeyWorkerAllocation getLatestAllocationForOffenderBooking(Long bookingId);
}
