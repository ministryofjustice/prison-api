package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import net.syscon.elite.api.resource.KeyWorkerResource;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.keyworker.KeyWorkerAllocationService;

import javax.ws.rs.Path;
import java.util.Collections;
import java.util.List;

@RestResource
@Path("/key-worker")
public class KeyWorkerResourceImpl implements KeyWorkerResource {
    private final KeyWorkerAllocationService keyWorkerService;

    public KeyWorkerResourceImpl(KeyWorkerAllocationService keyWorkerService) {
        this.keyWorkerService = keyWorkerService;
    }

    @Override
    public GetAvailableKeyworkersResponse getAvailableKeyworkers(String agencyId) {
        final List<Keyworker> availableKeyworkers = keyWorkerService.getAvailableKeyworkers(agencyId);

        return GetAvailableKeyworkersResponse.respond200WithApplicationJson(availableKeyworkers);
    }

    @Override
    public GetAllocationsForKeyworkerResponse getAllocationsForKeyworker(Long staffId, String agencyId) {
        final List<KeyWorkerAllocationDetail> allocationDetails = keyWorkerService.getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyId);

        return GetAllocationsForKeyworkerResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public GetKeyWorkerAgencyIdCurrentAllocationsResponse getKeyWorkerAgencyIdCurrentAllocations(String agencyId, List<Long> staffIds) {
        final List<KeyWorkerAllocationDetail> allocationDetails = keyWorkerService.getAllocationDetailsForKeyworkers(staffIds, agencyId);

        return GetKeyWorkerAgencyIdCurrentAllocationsResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public GetKeyWorkerAgencyIdCurrentAllocationsOffendersResponse getKeyWorkerAgencyIdCurrentAllocationsOffenders(String agencyId, List<String> offenderNos) {
        final List<KeyWorkerAllocationDetail> allocationDetails = keyWorkerService.getAllocationDetailsForOffenders(offenderNos, agencyId);

        return GetKeyWorkerAgencyIdCurrentAllocationsOffendersResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public PostKeyWorkerAgencyIdCurrentAllocationsResponse postKeyWorkerAgencyIdCurrentAllocations(String agencyId, List<Long> staffIds) {
        final List<KeyWorkerAllocationDetail> allocationDetails = keyWorkerService.getAllocationDetailsForKeyworkers(staffIds, agencyId);

        return PostKeyWorkerAgencyIdCurrentAllocationsResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse postKeyWorkerAgencyIdCurrentAllocationsOffenders(String agencyId, List<String> offenderNos) {
        final List<KeyWorkerAllocationDetail> allocationDetails = keyWorkerService.getAllocationDetailsForOffenders(offenderNos, agencyId);

        return PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public GetAllocationHistoryResponse getAllocationHistory(String agencyId, Long pageOffset, Long pageLimit) {
        PageRequest pageRequest = new PageRequest(pageOffset, pageLimit);
        Page<OffenderKeyWorker> allocations = keyWorkerService.getAllocationHistoryByAgency(agencyId, pageRequest);

        return GetAllocationHistoryResponse.respond200WithApplicationJson(allocations);
    }
}
