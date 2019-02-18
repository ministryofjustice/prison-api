package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/incidents"})
public interface IncidentsResource {

    @GET
    @Path("/{incidentId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return an Incident for a given incident ID", notes = "System access only",
            authorizations = { @Authorization("SYSTEM_USER"), @Authorization("SYSTEM_READ_ONLY") })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    IncidentResponse getIncident(@ApiParam(value = "Incident Id", required = true) @PathParam("incidentId") @NotNull Long incidentId);

    class IncidentResponse extends ResponseDelegate {
        public IncidentResponse(Response response, IncidentCase incidentCase) {
            super(response, incidentCase);
        }
    }

    class IncidentListResponse extends ResponseDelegate {
        public IncidentListResponse(Response response, List<IncidentCase> incidentCase) {
            super(response, incidentCase);
        }
    }

}
