package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.model.KeyWorkerAllocationDetail;
import net.syscon.prison.api.model.Keyworker;
import net.syscon.prison.api.model.OffenderKeyWorker;
import net.syscon.prison.api.resource.KeyWorkerResource;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.service.keyworker.KeyWorkerAllocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("${api.base.path}/key-worker")
public class KeyWorkerResourceImpl implements KeyWorkerResource {
    private final KeyWorkerAllocationService keyWorkerService;

    public KeyWorkerResourceImpl(final KeyWorkerAllocationService keyWorkerService) {
        this.keyWorkerService = keyWorkerService;
    }

    @Override
    public List<Keyworker> getAvailableKeyworkers(final String agencyId) {
        return keyWorkerService.getAvailableKeyworkers(agencyId);
    }

    @Override
    public List<KeyWorkerAllocationDetail> getAllocationsForKeyworker(final Long staffId, final String agencyId) {
        return keyWorkerService.getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyId);
    }

    @Override
    public List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocations(final String agencyId, final List<Long> staffIds) {
        return keyWorkerService.getAllocationDetailsForKeyworkers(staffIds, agencyId);
    }

    @Override
    public List<KeyWorkerAllocationDetail> postKeyWorkerAgencyIdCurrentAllocationsOffenders(final String agencyId, final List<String> offenderNos) {
        return keyWorkerService.getAllocationDetailsForOffenders(offenderNos, agencyId);
    }

    @Override
    public List<OffenderKeyWorker> postKeyWorkerOffendersAllocationHistory(final List<String> offenderNos) {
        return keyWorkerService.getAllocationHistoryByOffenderNos(offenderNos);
    }

    @Override
    public List<OffenderKeyWorker> postKeyWorkerStaffAllocationHistory(final List<Long> staffIds) {
        return keyWorkerService.getAllocationHistoryByStaffIds(staffIds);
    }

    @Override
    public ResponseEntity<List<OffenderKeyWorker>> getAllocationHistory(final String agencyId, final Long pageOffset, final Long pageLimit) {
        final var pageRequest = new PageRequest(pageOffset, pageLimit);
        final var allocations = keyWorkerService.getAllocationHistoryByAgency(agencyId, pageRequest);

        return ResponseEntity.ok()
                .headers(allocations.getPaginationHeaders())
                .body(allocations.getItems());
    }
}
