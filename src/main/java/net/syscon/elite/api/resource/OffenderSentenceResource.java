package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/offender-sentences"})
@SuppressWarnings("unused")
public interface OffenderSentenceResource {

    @GET
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "List of offenders (with associated sentence detail).", notes = "List of offenders (with associated sentence detail).", nickname = "getOffenderSentences")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderSentenceDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderSentencesResponse getOffenderSentences(@ApiParam(value = "agency/prison to restrict results, if none provided current active caseload will be used, unless offenderNo list is specified") @QueryParam("agencyId") String agencyId,
                                                      @ApiParam(value = "a list of offender numbers to search.") @QueryParam("offenderNo") List<String> offenderNo);

    @GET
    @Path("/home-detention-curfew-candidates")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "List of offenders Eligibile for HDC", notes = "Version 1", nickname = "getOffenderSentencesHomeDetentionCurfewCandidates")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sentence details for offenders who are candidates for Home Detention Curfew.", response = OffenderSentenceCalc.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderSentencesHomeDetentionCurfewCandidatesResponse getOffenderSentencesHomeDetentionCurfewCandidates(@ApiParam(value = "Include HD curfews having a checks passed date >= this date, when they have an assessment status. ISO-8601 date format. The default value is infinitely far in to the past") @QueryParam("minimumChecksPassedDateForAssessedCurfews") LocalDate minimumChecksPassedDateForAssessedCurfews);

    @GET
    @Path("/booking/{bookingId}/home-detention-curfews/latest")
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieve the current state of the latest Home Detention Curfew for a booking")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    Response getLatestHomeDetentionCurfew(@PathParam("bookingId") Long bookingId);

    @PUT
    @Path("/booking/{bookingId}/home-detention-curfews/latest/checks-passed")
    @Consumes({"application/json"})
    @ApiOperation(value = "Set the HDC checks passed flag")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The checks passed flag was set"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    Response setCurfewChecks(@PathParam("bookingId") Long bookingId, HdcChecks hdcChecks);

    @PUT
    @Path("/booking/{bookingId}/home-detention-curfews/latest/approval-status")
    @Consumes({"application/json"})
    @ApiOperation(value = "Set the HDC approval status")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The new approval status was set"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
    })
    Response setApprovalStatus(@PathParam("bookingId") Long bookingId, ApprovalStatus approvalStatus);

    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", notes = "Retrieves list of offenders (with associated sentence detail) - POST version to allow large offender lists.", nickname = "postOffenderSentences")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders is returned.", response = OffenderSentenceDetail.class, responseContainer = "List")})
    PostOffenderSentencesResponse postOffenderSentences(@ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @POST
    @Path("/bookings")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", notes = "Retrieves list of offenders (with associated sentence detail) - POST version using booking id lists.", nickname = "postOffenderSentencesBookings")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The list of offenders is returned.", response = OffenderSentenceDetail.class, responseContainer = "List")})
    PostOffenderSentencesBookingsResponse postOffenderSentencesBookings(@ApiParam(value = "The required booking ids (mandatory)", required = true) List<Long> body);

    @GET
    @Path("/booking/{bookingId}/sentenceTerms")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Sentence term details for a prisoner", nickname = "getOffenderSentenceTerms")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Sentence term details for a prisoner.", response = OffenderSentenceTerms.class),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List")})
    GetOffenderSentenceTermsResponse getOffenderSentenceTerms(@ApiParam(value = "The required booking id (mandatory)", required = true) @PathParam("bookingId") Long bookingId);

    class GetOffenderSentencesResponse extends ResponseDelegate {

        private GetOffenderSentencesResponse(final Response response) {
            super(response);
        }

        private GetOffenderSentencesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderSentencesResponse respond200WithApplicationJson(final List<OffenderSentenceDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentencesResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentencesResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentencesResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderSentenceTermsResponse extends ResponseDelegate {

        private GetOffenderSentenceTermsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderSentenceTermsResponse respond200WithApplicationJson(final OffenderSentenceTerms entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentenceTermsResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentenceTermsResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentenceTermsResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffenderSentencesHomeDetentionCurfewCandidatesResponse extends ResponseDelegate {

        private GetOffenderSentencesHomeDetentionCurfewCandidatesResponse(final Response response) {
            super(response);
        }

        private GetOffenderSentencesHomeDetentionCurfewCandidatesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffenderSentencesHomeDetentionCurfewCandidatesResponse respond200WithApplicationJson(final List<OffenderSentenceCalc> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesHomeDetentionCurfewCandidatesResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentencesHomeDetentionCurfewCandidatesResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesHomeDetentionCurfewCandidatesResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentencesHomeDetentionCurfewCandidatesResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesHomeDetentionCurfewCandidatesResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderSentencesHomeDetentionCurfewCandidatesResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderSentencesHomeDetentionCurfewCandidatesResponse(responseBuilder.build(), entity);
        }
    }

    class PostOffenderSentencesResponse extends ResponseDelegate {

        private PostOffenderSentencesResponse(final Response response) {
            super(response);
        }

        private PostOffenderSentencesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostOffenderSentencesResponse respond200WithApplicationJson(final List<OffenderSentenceDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostOffenderSentencesResponse(responseBuilder.build(), entity);
        }
    }

    class PostOffenderSentencesBookingsResponse extends ResponseDelegate {

        private PostOffenderSentencesBookingsResponse(final Response response) {
            super(response);
        }

        private PostOffenderSentencesBookingsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostOffenderSentencesBookingsResponse respond200WithApplicationJson(final List<OffenderSentenceDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostOffenderSentencesBookingsResponse(responseBuilder.build(), entity);
        }
    }
}
