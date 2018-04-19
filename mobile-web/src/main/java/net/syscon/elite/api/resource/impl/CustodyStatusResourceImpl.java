package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.CustodyStatusResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.CustodyStatusService;

import javax.ws.rs.Path;
import java.time.LocalDateTime;

@RestResource
@Path("/custody-statuses")
public class CustodyStatusResourceImpl implements CustodyStatusResource {

    private final CustodyStatusService custodyStatusService;

    public CustodyStatusResourceImpl(CustodyStatusService custodyStatusService) {
        this.custodyStatusService = custodyStatusService;
    }

    @Override
    public GetRecentMovementsResponse getRecentMovements(LocalDateTime fromDateTime) {
        return GetRecentMovementsResponse
                .respond200WithApplicationJson(custodyStatusService.getRecentMovements(fromDateTime));
    }
}
