package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.KeyWorkerResource;
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

    public KeyWorkerResourceImpl(final KeyWorkerAllocationService keyWorkerService) {
        this.keyWorkerService = keyWorkerService;
    }

    @Override
    public GetAvailableKeyworkersResponse getAvailableKeyworkers(final String agencyId) {
        final var availableKeyworkers = keyWorkerService.getAvailableKeyworkers(agencyId);

        return GetAvailableKeyworkersResponse.respond200WithApplicationJson(availableKeyworkers);
    }

    @Override
    public GetAllocationsForKeyworkerResponse getAllocationsForKeyworker(final Long staffId, final String agencyId) {
        final var allocationDetails = keyWorkerService.getAllocationDetailsForKeyworkers(Collections.singletonList(staffId), agencyId);

        return GetAllocationsForKeyworkerResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public PostKeyWorkerAgencyIdCurrentAllocationsResponse postKeyWorkerAgencyIdCurrentAllocations(final String agencyId, final List<Long> staffIds) {
        final var allocationDetails = keyWorkerService.getAllocationDetailsForKeyworkers(staffIds, agencyId);

        return PostKeyWorkerAgencyIdCurrentAllocationsResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse postKeyWorkerAgencyIdCurrentAllocationsOffenders(final String agencyId, final List<String> offenderNos) {
        final var allocationDetails = keyWorkerService.getAllocationDetailsForOffenders(offenderNos, agencyId);

        return PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse.respond200WithApplicationJson(allocationDetails);
    }

    @Override
    public PostKeyWorkerOffendersAllocationHistoryResponse postKeyWorkerOffendersAllocationHistory(final List<String> offenderNos) {
        final var allocHistory = keyWorkerService.getAllocationHistoryByOffenderNos(offenderNos);
        return PostKeyWorkerOffendersAllocationHistoryResponse.respond200WithApplicationJson(allocHistory);
    }

    @Override
    public PostKeyWorkerStaffAllocationHistoryResponse postKeyWorkerStaffAllocationHistory(final List<Long> staffIds) {
        final var allocHistory = keyWorkerService.getAllocationHistoryByStaffIds(staffIds);
        return PostKeyWorkerStaffAllocationHistoryResponse.respond200WithApplicationJson(allocHistory);
    }

    @Override
    public GetAllocationHistoryResponse getAllocationHistory(final String agencyId, final Long pageOffset, final Long pageLimit) {
        final var pageRequest = new PageRequest(pageOffset, pageLimit);
        final var allocations = keyWorkerService.getAllocationHistoryByAgency(agencyId, pageRequest);

        return GetAllocationHistoryResponse.respond200WithApplicationJson(allocations);
    }
}
