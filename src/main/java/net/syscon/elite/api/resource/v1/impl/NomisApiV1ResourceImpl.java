package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.resource.v1.NomisApiV1Resource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.v1.NomisApiV1Service;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RestResource
@Path("/v1")
public class NomisApiV1ResourceImpl implements NomisApiV1Resource {

    private final NomisApiV1Service service;

    public NomisApiV1ResourceImpl(NomisApiV1Service service) {
        this.service = service;
    }

    @Override
    public OffenderResponse getOffender(@NotNull String nomsId) {
        return new OffenderResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), service.getOffender(nomsId));

    }

    @Override
    public LatestBookingLocationResponse getLatestBookingLocation(@NotNull String nomsId) {
        return new LatestBookingLocationResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(), service.getLatestBookingLocation(nomsId));

    }

}
