package uk.gov.justice.hmpps.prison.repository;

import uk.gov.justice.hmpps.prison.api.model.KeyWorkerAllocationDetail;
import uk.gov.justice.hmpps.prison.api.model.Keyworker;
import uk.gov.justice.hmpps.prison.api.model.OffenderKeyWorker;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;

import java.util.List;
import java.util.Optional;

/**
 * Key Worker Allocation API repository interface.
 */
public interface KeyWorkerAllocationRepository {

    List<Keyworker> getAvailableKeyworkers(String agencyId);

    Optional<Keyworker> getKeyworkerDetailsByBooking(Long bookingId);

    List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworker(Long staffId, List<String> agencyIds);

    List<KeyWorkerAllocationDetail> getAllocationDetailsForKeyworkers(List<Long> staffIds, List<String> agencyIds);

    List<KeyWorkerAllocationDetail> getAllocationDetailsForOffenders(List<String> offenderNos, List<String> agencyIds);

    boolean checkKeyworkerExists(Long staffId);

    Page<OffenderKeyWorker> getAllocationHistoryByAgency(String agencyId, PageRequest pageRequest);

    List<OffenderKeyWorker> getAllocationHistoryByOffenderNos(List<String> offenderNos);

    List<OffenderKeyWorker> getAllocationHistoryByStaffIds(List<Long> staffIds);
}
