package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.IncidentsResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.impl.IncidentService;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RestResource
@Path("/incidents")
public class IncidentsResourceImpl implements IncidentsResource {

    private final IncidentService incidentService;

    public IncidentsResourceImpl(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    public IncidentResponse getIncident(Long incidentId) {
        return new IncidentResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), incidentService.getIncidentCase(incidentId));

    }

}
