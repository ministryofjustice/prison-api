package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;
import net.syscon.elite.api.resource.OffenderResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateAlertService;
import net.syscon.elite.service.impl.IncidentService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@RestResource
@Path("/offenders")
public class OffenderResourceImpl implements OffenderResource {

    private final IncidentService incidentService;
    private final InmateAlertService alertService;

    public OffenderResourceImpl(IncidentService incidentService, InmateAlertService alertService) {
        this.incidentService = incidentService;
        this.alertService = alertService;
    }

    @Override
    public IncidentListResponse getIncidentsByOffenderNo(@NotNull String offenderNo, String incidentType, List<String> participationRoles) {
        return new IncidentListResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(),
                incidentService.getIncidentCasesByOffenderNo(offenderNo, incidentType, participationRoles));
    }

    @Override
    public GetAlertsByOffenderNosResponse getAlertsByOffenderNo(@NotNull String offenderNo) {
        return GetAlertsByOffenderNosResponse.respond200WithApplicationJson(alertService.getInmateAlertsByOffenderNos(List.of(offenderNo)));
    }


}
