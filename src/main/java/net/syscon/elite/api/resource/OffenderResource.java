package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.model.adjudications.AdjudicationDetail;
import net.syscon.elite.api.model.adjudications.AdjudicationSearchResponse;
import net.syscon.elite.api.resource.BookingResource.GetAlertsByOffenderNosResponse;
import net.syscon.elite.api.resource.IncidentsResource.IncidentListResponse;
import net.syscon.elite.api.support.Order;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
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
    IncidentListResponse getIncidentsByOffenderNo(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathParam("offenderNo") @NotNull String offenderNo,
                                                  @ApiParam(value = "incidentType", example = "ASSAULT", allowMultiple = true) @QueryParam("incidentType") List<String> incidentTypes,
                                                  @ApiParam(value = "participationRoles", example = "ASSIAL", allowMultiple = true, allowableValues = "ACTINV,ASSIAL,FIGHT,IMPED,PERP,SUSASS,SUSINV,VICT,AI,PAS,AO") @QueryParam("participationRoles") List<String> participationRoles);

    @GET
    @Path("/{offenderNo}/addresses")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a list of addresses for a given offender, most recent first.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderAddress.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    List<OffenderAddress> getAddressesByOffenderNo(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathParam("offenderNo") @NotNull String offenderNo);

    @GET
    @Path("/{offenderNo}/adjudications")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a list of adjudications for a given offender")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AdjudicationSearchResponse.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    Response getAdjudicationsByOffenderNo(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathParam("offenderNo") @NotNull String offenderNo,
                                          @ApiParam(value = "An offence id to allow optionally filtering by type of offence") @QueryParam("offenceId") String offenceId,
                                          @ApiParam(value = "An agency id to allow optionally filtering by the agency in which the offence occurred") @QueryParam("agencyId") String agencyId,
                                          @ApiParam(value = "Adjudications must have been reported on or after this date (in YYYY-MM-DD format).") @QueryParam("fromDate") LocalDate fromDate,
                                          @ApiParam(value = "Adjudications must have been reported on or before this date (in YYYY-MM-DD format).") @QueryParam("toDate") LocalDate toDate,
                                          @ApiParam(value = "Requested offset of first record in returned collection of adjudications.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                          @ApiParam(value = "Requested limit to number of adjudications returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit);
    
    @GET
    @Path("/{offenderNo}/adjudications/{adjudicationNo}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Return a specific adjudication")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AdjudicationDetail.class),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class)})
    AdjudicationDetail getAdjudication(@ApiParam(value = "offenderNo", required = true, example = "A1234AA") @PathParam("offenderNo") @NotNull String offenderNo,
                             @ApiParam(value = "adjudicationNo", required = true) @PathParam("adjudicationNo") @NotNull long adjudicationNo);

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
    GetAlertsByOffenderNosResponse getAlertsByOffenderNo(@ApiParam(value = "Noms ID or Prisoner number", required = true, example = "A1234AA") @PathParam("offenderNo") @NotNull String offenderNo,
                                                         @ApiParam(value = "Only get alerts for the latest booking (prison term)") @QueryParam("latestOnly") Boolean latestOnly,
                                                         @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - " +
                                                                 "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active</p> ",
                                                                 required = true, example = "alertCode:eq:'XA',or:alertCode:eq:'RSS'") @QueryParam("query") String query,
                                                         @ApiParam(value = "Comma separated list of one or more Alert fields",
                                                                 allowableValues = "alertId, bookingId, alertType, alertCode, comment, dateCreated, dateExpires, active",
                                                                 defaultValue = "bookingId,alertType") @HeaderParam("Sort-Fields") String sortFields,
                                                         @ApiParam(value = "Sort order", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{offenderNo}/case-notes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender case notes", notes = "Retrieve an offenders case notes for latest booking", nickname = "getOffenderCaseNotes")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = CaseNote.class, responseContainer = "List")})
    Response getOffenderCaseNotes(  @ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") @PathParam("offenderNo") String offenderNo,
                                    @ApiParam(value = "start contact date to search from", required = true) @QueryParam("from") String from,
                                    @ApiParam(value = "end contact date to search up to (including this date)", required = true) @QueryParam("to") String to,
                                    @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - creationDateTime, type, subType, source</p> ", required = true) @QueryParam("query") String query,
                                    @ApiParam(value = "Requested offset of first record in returned collection of caseNote records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                    @ApiParam(value = "Requested limit to number of caseNote records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                    @ApiParam(value = "Comma separated list of one or more of the following fields - <b>creationDateTime, type, subType, source</b>") @HeaderParam("Sort-Fields") String sortFields,
                                    @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{offenderNo}/case-notes/{caseNoteId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender case note detail.", notes = "Retrieve an single offender case note", nickname = "getOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = CaseNote.class)})
    CaseNote getOffenderCaseNote(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true) @PathParam("offenderNo") String offenderNo,
                                                                    @ApiParam(value = "The case note id", required = true) @PathParam("caseNoteId") Long caseNoteId);

    @GET
    @Path("/{offenderNo}/sentence")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender Sentence Details", notes = "Retrieve an single offender sentence details", nickname = "getOffenderSentenceDetails")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "", response = OffenderSentenceDetail.class)})
    OffenderSentenceDetail getOffenderSentenceDetail(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true) @PathParam("offenderNo") String offenderNo);

    @POST
    @Path("/{offenderNo}/case-notes")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create case note for offender.", notes = "Create case note for offender. Will attach to the latest booking", nickname = "createOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The Case Note has been recorded. The updated object is returned including the status.", response = CaseNote.class),
            @ApiResponse(code = 409, message = "The case note has already been recorded under the booking. The current unmodified object (including status) is returned.", response = ErrorResponse.class)})
    CaseNote createOffenderCaseNote(@ApiParam(value = "The offenderNo of offender", required = true, example = "A1234AA") @PathParam("offenderNo") String offenderNo,
                                                                          @ApiParam(value = "", required = true) NewCaseNote body);

    @PUT
    @Path("/{offenderNo}/case-notes/{caseNoteId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Amend offender case note.", notes = "Amend offender case note.", nickname = "updateOffenderCaseNote")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Case Note amendment processed successfully. Updated case note is returned.", response = CaseNote.class),
            @ApiResponse(code = 400, message = "Invalid request - e.g. amendment text not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to amend case note.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Resource not found - offender or case note does not exist or is not accessible to user.", response = ErrorResponse.class),
            @ApiResponse(code = 500, message = "Internal server error.", response = ErrorResponse.class)})
    CaseNote updateOffenderCaseNote(@ApiParam(value = "Noms ID or Prisoner number (also called offenderNo)", required = true, example = "A1234AA") @PathParam("offenderNo") String offenderNo,
                                                                          @ApiParam(value = "The case note id", required = true, example = "1212134") @PathParam("caseNoteId") Long caseNoteId,
                                                                          @ApiParam(value = "", required = true) UpdateCaseNote body);
}
