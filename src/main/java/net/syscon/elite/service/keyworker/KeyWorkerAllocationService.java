package net.syscon.elite.service.keyworker;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;

import java.util.List;

/**
 * Key Worker Allocation service interface.
 */
public interface KeyWorkerAllocationService {

    Keyworker getKeyworkerDetailsByBooking(Long bookingId);

    List<Keyworker> getAvailableKeyworkers(String agencyId);

    List<KeyWorkerAllocationDetail> getAllocationsForCurrentCaseload(String username);

    List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(List<Long> staffIds, String agencyId);

    List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(List<String> offenderNos, String agencyId);

    Page<OffenderKeyWorker> getAllocationHistoryByAgency(String agencyId, PageRequest pageRequest);

    List<OffenderKeyWorker> getAllocationHistoryByStaffIds(List<Long> staffIds);

    List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(List<String> offenderNos);
}
