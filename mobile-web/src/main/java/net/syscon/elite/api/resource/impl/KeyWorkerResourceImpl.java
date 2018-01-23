package net.syscon.elite.api.resource.impl;

import com.google.common.collect.ImmutableSet;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.NewAllocation;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.resource.KeyWorkerResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.AgencyService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.KeyWorkerAllocationService;
import net.syscon.util.DateTimeConverter;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Path;
import java.time.LocalDate;
import java.util.Objects;
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
    public GetAllocatedOffendersResponse getAllocatedOffenders(String agencyId, String allocationType, String fromDateString, String toDateString, Long pageOffset, Long pageLimit, String sortFields, Order sortOrder) {
        Set<String> agencyFilter = buildAgencyFilter(agencyId);
        final LocalDate fromDate = DateTimeConverter.fromISO8601DateString(fromDateString);
        final LocalDate toDate = DateTimeConverter.fromISO8601DateString(toDateString);

        validateAllocatedOffenderListDateRange(fromDate, toDate);

        final Page<KeyWorkerAllocationDetail> allocatedOffenders = keyWorkerService.getAllocatedOffenders(agencyFilter, fromDate,
                toDate, allocationType, pageOffset, pageLimit, sortFields, sortOrder);

        return GetAllocatedOffendersResponse.respond200WithApplicationJson(allocatedOffenders);
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

    @Override
    public AllocateResponse allocate(NewAllocation body) {
        keyWorkerService.allocate(body);

        return AllocateResponse.respond201WithApplicationJson();
    }

    private void validateAllocatedOffenderListDateRange(LocalDate fromDate, LocalDate toDate) {
        // Validate date range
        if (Objects.nonNull(toDate) && toDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Invalid date range: toDate cannot be in the future.");
        }
        if (Objects.nonNull(fromDate) && Objects.nonNull(toDate) && toDate.isBefore(fromDate)) {
            throw new BadRequestException("Invalid date range: toDate is before fromDate.");
        }
    }
}
