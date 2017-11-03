package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.PrisonerCustodyStatus;
import net.syscon.elite.api.resource.CustodyStatusResource;
import net.syscon.elite.api.support.CustodyStatusCode;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CustodyStatusService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.ws.rs.Path;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
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
    public GetPrisonerCustodyStatusesResponse getPrisonerCustodyStatuses(String custodyStatusCodes, String sortFields, Order sortOrder) {
        final List<PrisonerCustodyStatus> custodyStatuses = custodyStatusService.listCustodyStatuses(asValidCustodyStatusCode(custodyStatusCodes), sortOrder);
        return GetPrisonerCustodyStatusesResponse.respond200WithApplicationJson(custodyStatuses);
    }

    @Override
    @PreAuthorize("authentication.authorities.?[authority.contains('_ADMIN')].size() != 0 || authentication.authorities.?[authority.contains('GLOBAL_SEARCH')].size() != 0")
    public GetPrisonerCustodyStatusResponse getPrisonerCustodyStatus(String offenderNo) {
        final PrisonerCustodyStatus custodyStatus = custodyStatusService.getCustodyStatus(offenderNo);
        return GetPrisonerCustodyStatusResponse.respond200WithApplicationJson(custodyStatus);
    }

    private List<CustodyStatusCode> asValidCustodyStatusCode(String codes) {
        return StringUtils.isNotBlank(codes) ?
                Arrays.stream(codes.split(",")).map(CustodyStatusCode::valueOf).collect(Collectors.toList()) :
                Arrays.asList();
    }
}
