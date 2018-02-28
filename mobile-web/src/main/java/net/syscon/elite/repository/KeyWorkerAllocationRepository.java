package net.syscon.elite.repository;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.repository.impl.KeyWorkerAllocation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Key Worker Allocation API repository interface.
 */
public interface KeyWorkerAllocationRepository {
    void createAllocation(KeyWorkerAllocation allocation, String username);

    void deactivateAllocationForOffenderBooking(Long bookingId, String reason, String username);

    Optional<KeyWorkerAllocation> getCurrentAllocationForOffenderBooking(Long bookingId);

    List<KeyWorkerAllocation> getAllocationHistoryForPrisoner(Long bookingId, String orderByFields, Order order);

    Optional<KeyWorkerAllocation> getLatestAllocationForOffenderBooking(Long bookingId);

    void deactivateAllocationsForKeyWorker(Long staffId, String reason, String username);

    Page<OffenderSummary> getUnallocatedOffenders(Set<String> agencyIds, Long offset, Long limit, String sortFields, Order sortOrder);

    List<Keyworker> getAvailableKeyworkers(String agencyId);

    void checkAvailableKeyworker(Long bookingId, Long staffId);

    Page<KeyWorkerAllocationDetail> getAllocatedOffenders(Set<String> agencyIds, LocalDate fromDate, LocalDate toDate, String type, Long offset, Long limit, String sortFields, Order sortOrder);

    Optional<Keyworker> getKeyworkerDetails(Long staffId);

    List<KeyWorkerAllocation> getAllocationsForKeyworker(Long staffId);

    List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworker(Long staffId);

    boolean checkKeyworkerExists(Long staffId);
}
