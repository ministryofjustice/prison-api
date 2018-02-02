package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.resource.KeyWorkerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.KeyWorkerAllocationService;

import javax.ws.rs.Path;
import java.util.List;

import static net.syscon.util.DateTimeConverter.fromISO8601DateString;

@RestResource
@Path("/key-worker")
public class KeyWorkerResourceImpl implements KeyWorkerResource {
    private final KeyWorkerAllocationService keyWorkerService;

    public KeyWorkerResourceImpl(KeyWorkerAllocationService keyWorkerService) {
        this.keyWorkerService = keyWorkerService;
    }

    @Override
    public GetAllocationsResponse getAllocations(String agencyId, String allocationType, String fromDate, String toDate, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        final Page<KeyWorkerAllocationDetail> allocations = keyWorkerService.getAllocations(
                agencyId,
                fromISO8601DateString(fromDate),
                fromISO8601DateString(toDate),
                allocationType,
                pageOffset,
                pageLimit,
                sortFields,
                sortOrder);

        return GetAllocationsResponse.respond200WithApplicationJson(allocations);
    }

    @Override
    public GetUnallocatedOffendersResponse getUnallocatedOffenders(String agencyId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        final Page<OffenderSummary> unallocatedOffenders = keyWorkerService.getUnallocatedOffenders(
                agencyId,
                pageOffset,
                pageLimit,
                sortFields,
                sortOrder);

        return GetUnallocatedOffendersResponse.respond200WithApplicationJson(unallocatedOffenders);
    }

    @Override
    public AllocateResponse allocate(NewAllocation body) {
        keyWorkerService.allocate(body);

        return AllocateResponse.respond201WithApplicationJson();
    }

    @Override
    public GetAvailableKeyworkersResponse getAvailableKeyworkers(String agencyId) {
        final List<Keyworker> availableKeyworkers = keyWorkerService.getAvailableKeyworkers(agencyId);

        return GetAvailableKeyworkersResponse.respond200WithApplicationJson(availableKeyworkers);
    }

    @Override
    public GetKeyworkerDetailsResponse getKeyworkerDetails(Long staffId) {
        Keyworker keyWorker = keyWorkerService.getKeyworkerDetails(staffId);

        return GetKeyworkerDetailsResponse.respond200WithApplicationJson(keyWorker);
    }
}
