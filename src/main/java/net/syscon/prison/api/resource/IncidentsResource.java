package net.syscon.prison.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.IncidentCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.validation.constraints.NotNull;

@Api(tags = {"/incidents"})
public interface IncidentsResource {

    @GetMapping("/{incidentId}")
    @ApiOperation(value = "Return an Incident for a given incident ID", notes = "System access only")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    IncidentCase getIncident(@ApiParam(value = "Incident Id", required = true) @PathVariable("incidentId") @NotNull Long incidentId);

}
