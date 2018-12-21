package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/case-notes"})
@SuppressWarnings("unused")
public interface CaseNoteResource {

    @GET
    @Path("/staff-usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname="getCaseNoteStaffUsageSummary")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = CaseNoteStaffUsage.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetCaseNoteStaffUsageSummaryResponse getCaseNoteStaffUsageSummary(@ApiParam(value = "a list of staffId numbers to use.", required = true) @QueryParam("staffId") List<String> staffId,
                                                                      @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") @QueryParam("numMonths") Integer numMonths,
                                                                      @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") @QueryParam("fromDate") LocalDate fromDate,
                                                                      @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") @QueryParam("toDate") LocalDate toDate,
                                                                      @ApiParam(value = "Case note type.") @QueryParam("type") String type,
                                                                      @ApiParam(value = "Case note sub-type.") @QueryParam("subType") String subType);

    @GET
    @Path("/usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Count of case notes", notes = "Count of case notes", nickname="getCaseNoteUsageSummary")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = CaseNoteUsage.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetCaseNoteUsageSummaryResponse getCaseNoteUsageSummary(@ApiParam(value = "a list of offender numbers to search.", required = true) @QueryParam("offenderNo") List<String> offenderNo,
                                                            @ApiParam(value = "Staff Id to filter by") @QueryParam("staffId") Integer staffId,
                                                            @ApiParam(value = "Agency Id to filter by") @QueryParam("agencyId") String agencyId,
                                                            @ApiParam(value = "Number of month to look forward (if fromDate only defined), or back (if toDate only defined). Default is 1 month", defaultValue = "1") @QueryParam("numMonths") Integer numMonths,
                                                            @ApiParam(value = "Only case notes occurring on or after this date (in YYYY-MM-DD format) will be considered.  If not defined then the numMonth before the current date, unless a toDate is defined when it will be numMonths before toDate") @QueryParam("fromDate") LocalDate fromDate,
                                                            @ApiParam(value = "Only case notes occurring on or before this date (in YYYY-MM-DD format) will be considered. If not defined then the current date will be used, unless a fromDate is defined when it will be numMonths after fromDate") @QueryParam("toDate") LocalDate toDate,
                                                            @ApiParam(value = "Case note type.") @QueryParam("type") String type,
                                                            @ApiParam(value = "Case note sub-type.") @QueryParam("subType") String subType);

    @POST
    @Path("/staff-usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves list of case notes grouped by type/sub-type and staff", notes = "Retrieves list of case notes grouped by type/sub-type and staff", nickname="getCaseNoteStaffUsageSummaryByPost")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The case note usage list is returned.", response = CaseNoteStaffUsage.class, responseContainer = "List") })
    GetCaseNoteStaffUsageSummaryByPostResponse getCaseNoteStaffUsageSummaryByPost(@ApiParam(value = "", required = true) CaseNoteStaffUsageRequest body);

    @POST
    @Path("/usage")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves list of case notes grouped by type and offender", notes = "Retrieves list of case notes grouped by type and offender", nickname="getCaseNoteUsageSummaryByPost")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The case note usage list is returned.", response = CaseNoteUsage.class, responseContainer = "List") })
    GetCaseNoteUsageSummaryByPostResponse getCaseNoteUsageSummaryByPost(@ApiParam(value = "", required = true) CaseNoteUsageRequest body);

    class GetCaseNoteStaffUsageSummaryResponse extends ResponseDelegate {

        private GetCaseNoteStaffUsageSummaryResponse(Response response) { super(response); }
        private GetCaseNoteStaffUsageSummaryResponse(Response response, Object entity) { super(response, entity); }

        public static GetCaseNoteStaffUsageSummaryResponse respond200WithApplicationJson(List<CaseNoteStaffUsage> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteStaffUsageSummaryResponse(responseBuilder.build(), entity);
        }

        public static GetCaseNoteStaffUsageSummaryResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteStaffUsageSummaryResponse(responseBuilder.build(), entity);
        }

        public static GetCaseNoteStaffUsageSummaryResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteStaffUsageSummaryResponse(responseBuilder.build(), entity);
        }

        public static GetCaseNoteStaffUsageSummaryResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteStaffUsageSummaryResponse(responseBuilder.build(), entity);
        }
    }

    class GetCaseNoteUsageSummaryResponse extends ResponseDelegate {

        private GetCaseNoteUsageSummaryResponse(Response response) { super(response); }
        private GetCaseNoteUsageSummaryResponse(Response response, Object entity) { super(response, entity); }

        public static GetCaseNoteUsageSummaryResponse respond200WithApplicationJson(List<CaseNoteUsage> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteUsageSummaryResponse(responseBuilder.build(), entity);
        }

        public static GetCaseNoteUsageSummaryResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteUsageSummaryResponse(responseBuilder.build(), entity);
        }

        public static GetCaseNoteUsageSummaryResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteUsageSummaryResponse(responseBuilder.build(), entity);
        }

        public static GetCaseNoteUsageSummaryResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteUsageSummaryResponse(responseBuilder.build(), entity);
        }
    }

    class GetCaseNoteStaffUsageSummaryByPostResponse extends ResponseDelegate {

        private GetCaseNoteStaffUsageSummaryByPostResponse(Response response) { super(response); }
        private GetCaseNoteStaffUsageSummaryByPostResponse(Response response, Object entity) { super(response, entity); }

        public static GetCaseNoteStaffUsageSummaryByPostResponse respond200WithApplicationJson(List<CaseNoteStaffUsage> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteStaffUsageSummaryByPostResponse(responseBuilder.build(), entity);
        }
    }

    class GetCaseNoteUsageSummaryByPostResponse extends ResponseDelegate {

        private GetCaseNoteUsageSummaryByPostResponse(Response response) { super(response); }
        private GetCaseNoteUsageSummaryByPostResponse(Response response, Object entity) { super(response, entity); }

        public static GetCaseNoteUsageSummaryByPostResponse respond200WithApplicationJson(List<CaseNoteUsage> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetCaseNoteUsageSummaryByPostResponse(responseBuilder.build(), entity);
        }
    }
}
