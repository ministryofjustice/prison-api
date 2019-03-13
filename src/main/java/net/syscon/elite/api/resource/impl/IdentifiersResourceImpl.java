package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.resource.IdentifiersResource;
import net.syscon.elite.core.RestResource;
import net.syscon.elite.service.InmateService;

import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RestResource
@Path("/identifiers")
public class IdentifiersResourceImpl implements IdentifiersResource {
    private final InmateService inmateService;

    public IdentifiersResourceImpl(final InmateService inmateService) {
        this.inmateService = inmateService;
    }

    @Override
    public IdentifiersListResponse getOffenderIdentifiersByTypeAndValue(final String identifierType, final String identifierValue) {
        return new IdentifiersListResponse(Response.status(200)
                .header("Content-Type", MediaType.APPLICATION_JSON).build(),
                inmateService.getOffenderIdentifiersByTypeAndValue(identifierType, identifierValue));
    }
}
