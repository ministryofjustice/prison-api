package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.resource.CustodyStatusResource;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CustodyStatusService;
import net.syscon.util.DateTimeConverter;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestResource
@Path("/custody-statuses")
public class CustodyStatusResourceImpl implements CustodyStatusResource {

    private final CustodyStatusService custodyStatusService;

    public CustodyStatusResourceImpl(CustodyStatusService custodyStatusService) {
        this.custodyStatusService = custodyStatusService;
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0 || authentication.authorities.?[authority.contains('GLOBAL_SEARCH')].size() != 0")
    public GetPrisonerCustodyStatusesResponse getPrisonerCustodyStatuses(List<String> custodyStatusCodes, String sortFields, Order sortOrder, String onDateString) {
        LocalDate onDate = DateTimeConverter.fromISO8601DateString(onDateString);
        if (onDate == null) {
            onDate = LocalDate.now();
        }

        final List<CustodyStatusCode> statusCodes = custodyStatusCodes.stream()
                .map(CustodyStatusCode::valueOf)
                .collect(Collectors.toList());

        final List<PrisonerCustodyStatus> custodyStatuses = custodyStatusService.listCustodyStatuses(statusCodes, onDate, sortOrder);
        return GetPrisonerCustodyStatusesResponse.respond200WithApplicationJson(custodyStatuses);
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0 || authentication.authorities.?[authority.contains('GLOBAL_SEARCH')].size() != 0")
    public GetPrisonerCustodyStatusResponse getPrisonerCustodyStatus(String offenderNo) {
        final PrisonerCustodyStatus custodyStatus = custodyStatusService.getCustodyStatus(offenderNo, LocalDate.now());
        return GetPrisonerCustodyStatusResponse.respond200WithApplicationJson(custodyStatus);
    }
}
