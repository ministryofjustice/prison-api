package net.syscon.elite.api.resource.impl;

import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.resource.KeyWorkerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.KeyWorkerAllocationService;

import javax.ws.rs.Path;
import java.util.Set;

@RestResource
@Path("/key-worker")
public class KeyWorkerResourceImpl implements KeyWorkerResource {
    private final AgencyService agencyService;
    private final KeyWorkerAllocationService keyWorkerService;

    public KeyWorkerResourceImpl(AgencyService agencyService, KeyWorkerAllocationService keyWorkerService) {
        this.agencyService = agencyService;
        this.keyWorkerService = keyWorkerService;
    }

    @Override
    public GetUnallocatedOffendersResponse getUnallocatedOffenders(String agencyId, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Set<String> agencyFilter = buildAgencyFilter(agencyId);

        final Page<OffenderSummary> unallocatedOffenders = keyWorkerService.getUnallocatedOffenders(agencyFilter, pageOffset, pageLimit, sortFields, sortOrder);

        return GetUnallocatedOffendersResponse.respond200WithApplicationJson(unallocatedOffenders);
    }

    private Set<String> buildAgencyFilter(String agencyId) {
        final Set<String> allowedAgencyIds = agencyService.getAgencyIds();
        if(agencyId != null){
            if(!allowedAgencyIds.contains(agencyId)){
                throw EntityNotFoundException.withMessage(String.format("Agency with id %s not found.", agencyId));
            }
            return ImmutableSet.of(agencyId);
        }
        return allowedAgencyIds;
    }
}
