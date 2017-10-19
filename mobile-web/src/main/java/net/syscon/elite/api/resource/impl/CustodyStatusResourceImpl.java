package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CustodyStatus;
import net.syscon.elite.api.resource.CustodyStatusResource;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CustodyStatusService;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;

import java.util.List;

@RestResource
@Path("/custody-statuses")
public class CustodyStatusResourceImpl implements CustodyStatusResource {

    private final CustodyStatusService custodyStatusService;

    public CustodyStatusResourceImpl(CustodyStatusService custodyStatusService) {
        this.custodyStatusService = custodyStatusService;
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0 || authentication.authorities.?[authority.contains('GLOBAL_SEARCH')].size() != 0")
    public GetCustodyStatusesResponse getCustodyStatuses(String query, String sortFields, Order sortOrder) {
        final List<CustodyStatus> custodyStatuses = custodyStatusService.listCustodyStatuses(query, sortFields, sortOrder);
        return GetCustodyStatusesResponse.respond200WithApplicationJson(custodyStatuses);
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0 || authentication.authorities.?[authority.contains('GLOBAL_SEARCH')].size() != 0")
    public GetCustodyStatusResponse getCustodyStatus(String offenderNo) {
        final CustodyStatus custodyStatus = custodyStatusService.getCustodyStatus(offenderNo);
        return GetCustodyStatusResponse.respond200WithApplicationJson(custodyStatus);
    }
}
