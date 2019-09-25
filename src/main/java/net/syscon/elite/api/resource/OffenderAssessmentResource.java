package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.ResponseDelegate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Api(tags = {"/offender-assessments"})
@SuppressWarnings("unused")
public interface OffenderAssessmentResource {

    @GET
    @Path("/{assessmentCode}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Offender assessment detail for multiple offenders.", notes = "Offender assessment detail for multiple offenders.", nickname = "getOffenderAssessmentsAssessmentCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderAssessmentsAssessmentCodeResponse getOffenderAssessmentsAssessmentCode(@ApiParam(value = "Assessment Type Code", required = true) @PathParam("assessmentCode") String assessmentCode,
                                                                                      @ApiParam(value = "The required offender numbers", required = true) @QueryParam("offenderNo") List<String> offenderNo,
                                                                                      @ApiParam(value = "Returns only the assessments for the current sentence if true, otherwise all previous sentences are included", defaultValue = "true") @QueryParam("latestOnly") Boolean latestOnly,
                                                                                      @ApiParam(value = "Returns only active assessments if true, otherwise assessments with any status are included", defaultValue = "true") @QueryParam("activeOnly") Boolean activeOnly);

    @POST
    @Path("/{assessmentCode}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.", notes = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.", nickname = "postOffenderAssessmentsAssessmentCode")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The assessment list is returned.", response = Assessment.class, responseContainer = "List")})
    PostOffenderAssessmentsAssessmentCodeResponse postOffenderAssessmentsAssessmentCode(@ApiParam(value = "Assessment Type Code", required = true) @PathParam("assessmentCode") String assessmentCode,
                                                                                        @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body,
                                                                                        @ApiParam(value = "Returns only the assessments for the current sentence if true, otherwise all previous sentences are included", defaultValue = "true") @QueryParam("latestOnly") Boolean latestOnly,
                                                                                        @ApiParam(value = "Returns only active assessments if true, otherwise assessments with any status are included", defaultValue = "true") @QueryParam("activeOnly") Boolean activeOnly);


    @POST
    @Path("/csra/list")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves Offender CRSAs for multiple offenders - POST version to allow large offender lists.", notes = "Retrieves Offender CRSAs for multiple offenders - POST version to allow large offender lists.", nickname = "postOffenderAssessmentsCsraList")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The CSRA assessment list is returned, 1 per offender.", response = Assessment.class, responseContainer = "List")})
    PostOffenderAssessmentsCsraListResponse postOffenderAssessmentsCsraList(@ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @GET
    @Path("/category/{agencyId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Returns category information on Offenders at a prison.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderCategorise.class, responseContainer = "List")})
    List<OffenderCategorise> getOffenderCategorisations(@ApiParam(value = "Prison id", required = true) @PathParam("agencyId") String agencyId,
                                                        @ApiParam(value = "Indicates which type of category information is required." +
                                                                "<li>UNCATEGORISED: Offenders who need to be categorised,</li>" +
                                                                "<li>CATEGORISED: Offenders who have an approved categorisation,</li>" +
                                                                "<li>RECATEGORISATIONS: Offenders who will soon require recategorisation</li>", required = true) @QueryParam("type") @NotNull(message = "Categorisation type must not be null") String type,
                                                        @ApiParam(value = "For type CATEGORISED: The past date from which categorisations are returned.<br />" +
                                                                "For type RECATEGORISATIONS: the future cutoff date: list includes all prisoners who require recategorisation on or before this date.<br />" +
                                                                "For type UNCATEGORISED: Ignored; do not set this parameter.") @QueryParam("date") LocalDate date);

    @POST
    @Path("/category/{agencyId}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Returns Categorisation details for supplied Offenders - POST version to allow large offender lists.", notes = "Categorisation details for supplied Offenders", nickname = "getOffenderCategorisations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders with categorisation details is returned if categorisation record exists", response = OffenderCategorise.class, responseContainer = "List")})
    List<OffenderCategorise> getOffenderCategorisations(@ApiParam(value = "Prison id", required = true) @PathParam("agencyId") String agencyId,
                                                        @ApiParam(value = "The required booking Ids (mandatory)", required = true) Set<Long> bookingIds,
                                                        @ApiParam(value = "Only get the latest category for each booking", defaultValue = "true") @QueryParam("latestOnly") Boolean latestOnly);

    @POST
    @Path("/category/categorise")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Record new offender categorisation.", notes = "Create new categorisation record.", nickname = "createCategorisation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to categorise the offender.", response = ErrorResponse.class)})
    Response createCategorisation(@ApiParam(value = "Categorisation details", required = true) @Valid CategorisationDetail body);

    @PUT
    @Path("/category/approve")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Approve a pending offender categorisation.", notes = "Update categorisation record with approval.", nickname = "approveCategorisation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to approve the categorisation.", response = ErrorResponse.class)})
    Response approveCategorisation(@ApiParam(value = "Approval details", required = true) @Valid CategoryApprovalDetail body);

    @PUT
    @Path("/category/{bookingId}/nextReviewDate/{nextReviewDate}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Update the next review date on the latest active categorisation", notes = "Update categorisation record with new next review date.", nickname = "updateCategorisationNextReviewDate")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 404, message = "Active categorisation not found.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update the categorisation.", response = ErrorResponse.class)})
    Response updateCategorisationNextReviewDate(@ApiParam(value = "The booking id of offender", required = true) @PathParam("bookingId") Long bookingId,
                                                @ApiParam(value = "The new next review date (in YYYY-MM-DD format)", required = true) @PathParam("nextReviewDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextReviewDate);

    class GetOffenderAssessmentsAssessmentCodeResponse extends ResponseDelegate {

        private GetOffenderAssessmentsAssessmentCodeResponse(final Response response) {
            super(response);
        }

        private GetOffenderAssessmentsAssessmentCodeResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond200WithApplicationJson(final List<Assessment> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }
    }

    class PostOffenderAssessmentsAssessmentCodeResponse extends ResponseDelegate {

        private PostOffenderAssessmentsAssessmentCodeResponse(final Response response) {
            super(response);
        }

        private PostOffenderAssessmentsAssessmentCodeResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostOffenderAssessmentsAssessmentCodeResponse respond200WithApplicationJson(final List<Assessment> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }
    }

    class PostOffenderAssessmentsCsraListResponse extends ResponseDelegate {

        private PostOffenderAssessmentsCsraListResponse(final Response response) {
            super(response);
        }

        private PostOffenderAssessmentsCsraListResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostOffenderAssessmentsCsraListResponse respond200WithApplicationJson(final List<Assessment> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostOffenderAssessmentsCsraListResponse(responseBuilder.build(), entity);
        }
    }
}
