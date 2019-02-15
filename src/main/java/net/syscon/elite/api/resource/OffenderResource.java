package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import java.util.List;

@Api(tags = {"/offenders"})
public interface OffenderResource {

    @GET
    @Path("/{offenderNo}/incidents")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a set Incidents for a given offender No.",
            notes = "Can be filtered by participation type and incident type",
            authorizations = { @Authorization("SYSTEM_USER"), @Authorization("SYSTEM_READ_ONLY") })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    IncidentListResponse getIncidentsByOffenderNo(@ApiParam(value = "offenderNo", required = true) @PathParam("offenderNo") @NotNull String offenderNo,
                                                                    @ApiParam(value = "incidentType", example = "ASSAULT") @QueryParam("incidentType") String incidentType,
                                                                    @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") @QueryParam("participationRoles") List<String> participationRoles) ;


    @GET
    @Path("/{offenderNo}/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a list of alerts for a given offender No.",  notes = "System access only",
            authorizations = { @Authorization("SYSTEM_USER"), @Authorization("SYSTEM_READ_ONLY") })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})

    GetAlertsByOffenderNosResponse getAlertsByOffenderNo(@ApiParam(value = "offenderNo", required = true) @PathParam("offenderNo") @NotNull String offenderNo);
}
