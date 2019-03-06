package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/offender-assessments"})
@SuppressWarnings("unused")
public interface OffenderAssessmentResource {

    @GET
    @Path("/{assessmentCode}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Offender assessment detail for multiple offenders.", notes = "Offender assessment detail for multiple offenders.", nickname="getOffenderAssessmentsAssessmentCode")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Assessment.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetOffenderAssessmentsAssessmentCodeResponse getOffenderAssessmentsAssessmentCode(@ApiParam(value = "Assessment Type Code", required = true) @PathParam("assessmentCode") String assessmentCode,
                                                                                      @ApiParam(value = "The required offender numbers", required = true) @QueryParam("offenderNo") List<String> offenderNo,
                                                                                      @ApiParam(value = "Returns only the assessments for the current sentence if true, otherwise all previous sentences are included", defaultValue = "true") @QueryParam("latestOnly") Boolean latestOnly);

    @POST
    @Path("/{assessmentCode}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.", notes = "Retrieves Offender assessment details for multiple offenders - POST version to allow large offender lists.", nickname="postOffenderAssessmentsAssessmentCode")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The assessment list is returned.", response = Assessment.class, responseContainer = "List") })
    PostOffenderAssessmentsAssessmentCodeResponse postOffenderAssessmentsAssessmentCode(@ApiParam(value = "Assessment Type Code", required = true) @PathParam("assessmentCode") String assessmentCode,
                                                                                        @ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @POST
    @Path("/csra/list")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Retrieves Offender CRSAs for multiple offenders - POST version to allow large offender lists.", notes = "Retrieves Offender CRSAs for multiple offenders - POST version to allow large offender lists.", nickname="postOffenderAssessmentsCsraList")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The CSRA assessment list is returned, 1 per offender.", response = Assessment.class, responseContainer = "List") })
    PostOffenderAssessmentsCsraListResponse postOffenderAssessmentsCsraList(@ApiParam(value = "The required offender numbers (mandatory)", required = true) List<String> body);

    @GET
    @Path("/category/{agencyId}/uncategorised")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Offenders who need to be categorised.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderCategorise.class, responseContainer = "List") })
    GetUncategorisedResponse getUncategorised(@ApiParam(value = "Prison id", required = true) @PathParam("agencyId")String agencyId);

    @GET
    @Path("/category/{agencyId}/categorised")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Offenders who have an approved categorisation.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderCategorise.class, responseContainer = "List") })
    GetUncategorisedResponse getApprovedCategorised(@ApiParam(value = "Prison id", required = true) @PathParam("agencyId")String agencyId,
                                                    @ApiParam(value = "The date from which categorisations are returned", required = false) @QueryParam("fromDate") LocalDate fromDate);



    @POST
    @Path("/category/categorise")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Record new offender categorisation.", notes = "Create new categorisation record.", nickname="createCategorisation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to categorise the offender.", response = ErrorResponse.class) })
    CreateCategorisationResponse createCategorisation(@ApiParam(value = "Categorisation details", required = true) @Valid CategorisationDetail body);

    @PUT
    @Path("/category/approve")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Approve a pending offender categorisation.", notes = "Update categorisation record with approval.", nickname="approveCategorisation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to approve the categorisation.", response = ErrorResponse.class) })
    Response approveCategorisation(@ApiParam(value = "Approval details", required = true) @Valid CategoryApprovalDetail body);

    class GetOffenderAssessmentsAssessmentCodeResponse extends ResponseDelegate {

        private GetOffenderAssessmentsAssessmentCodeResponse(Response response) { super(response); }
        private GetOffenderAssessmentsAssessmentCodeResponse(Response response, Object entity) { super(response, entity); }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond200WithApplicationJson(List<Assessment> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }

        public static GetOffenderAssessmentsAssessmentCodeResponse respond500WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }
    }

    class PostOffenderAssessmentsAssessmentCodeResponse extends ResponseDelegate {

        private PostOffenderAssessmentsAssessmentCodeResponse(Response response) { super(response); }
        private PostOffenderAssessmentsAssessmentCodeResponse(Response response, Object entity) { super(response, entity); }

        public static PostOffenderAssessmentsAssessmentCodeResponse respond200WithApplicationJson(List<Assessment> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostOffenderAssessmentsAssessmentCodeResponse(responseBuilder.build(), entity);
        }
    }

    class PostOffenderAssessmentsCsraListResponse extends ResponseDelegate {

        private PostOffenderAssessmentsCsraListResponse(Response response) { super(response); }
        private PostOffenderAssessmentsCsraListResponse(Response response, Object entity) { super(response, entity); }

        public static PostOffenderAssessmentsCsraListResponse respond200WithApplicationJson(List<Assessment> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostOffenderAssessmentsCsraListResponse(responseBuilder.build(), entity);
        }
    }

    class GetUncategorisedResponse extends ResponseDelegate {

        private GetUncategorisedResponse(Response response) { super(response); }
        private GetUncategorisedResponse(Response response, Object entity) { super(response, entity); }

        public static GetUncategorisedResponse respond200WithApplicationJson(List<OffenderCategorise> entity) {
            ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetUncategorisedResponse(responseBuilder.build(), entity);
        }
    }

    class CreateCategorisationResponse extends ResponseDelegate {

        public CreateCategorisationResponse(Response response) { super(response); }

        public static OffenderAssessmentResource.CreateCategorisationResponse respond201WithApplicationJson() {
            ResponseBuilder responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            return new OffenderAssessmentResource.CreateCategorisationResponse(responseBuilder.build());
        }
    }
}
