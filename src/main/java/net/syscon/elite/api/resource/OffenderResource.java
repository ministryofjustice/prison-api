package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.IncidentCase;
import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;
import net.syscon.elite.api.support.Order;

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
            authorizations = {@Authorization("SYSTEM_USER"), @Authorization("SYSTEM_READ_ONLY")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = IncidentCase.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    IncidentListResponse getIncidentsByOffenderNo(@ApiParam(value = "offenderNo", required = true) @PathParam("offenderNo") @NotNull String offenderNo,
                                                  @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) @QueryParam("incidentType") List<String> incidentTypes,
                                                  @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") @QueryParam("participationRoles") List<String> participationRoles);


    @GET
    @Path("/{offenderNo}/alerts")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a list of alerts for a given offender No.", notes = "System or cat tool access only",
            authorizations = {@Authorization("SYSTEM_USER"), @Authorization("SYSTEM_READ_ONLY"), @Authorization("CREATE_CATEGORISATION"), @Authorization("APPROVE_CATEGORISATION")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Alert.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    GetAlertsByOffenderNosResponse getAlertsByOffenderNo(@ApiParam(value = "Noms or Prison number", required = true) @PathParam("offenderNo") @NotNull String offenderNo,
                                                         @ApiParam(value = "Only get alerts for the latest booking (prison term)") @QueryParam("latestOnly") Boolean latestOnly,
                                                         @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - " +
                                                                 "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active</p> ",
                                                                 required = true, example = "alertCode:eq:'XA',or:alertCode:eq:'RSS'") @QueryParam("query") String query,
                                                         @ApiParam(value = "Comma separated list of one or more Alert fields",
                                                                 allowableValues = "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active",
                                                                 defaultValue = "bookingId,alertType") @HeaderParam("Sort-Fields") String sortFields,
                                                         @ApiParam(value = "Sort order", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);
}
