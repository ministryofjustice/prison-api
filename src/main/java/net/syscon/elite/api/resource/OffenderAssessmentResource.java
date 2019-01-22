package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.Assessment;
import net.syscon.elite.api.model.CategorisationDetail;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.OffenderCategorise;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
                                                                                      @ApiParam(value = "The required offender numbers", required = true) @QueryParam("offenderNo") List<String> offenderNo);

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


    @POST
    @Path("/category/categorise")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Record new offender categorisation.", notes = "Create new categorisation record.", nickname="createCategorisation")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. category does not exist.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to categorise the offender.", response = ErrorResponse.class) })
    CreateCategorisationResponse createCategorisation(@ApiParam(value = "", required = true) @Valid CategorisationDetail body);

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

        private CreateCategorisationResponse(Response response) { super(response); }
        private CreateCategorisationResponse(Response response, Object entity) { super(response, entity); }

        public static OffenderAssessmentResource.CreateCategorisationResponse respond201WithApplicationJson() {
            ResponseBuilder responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            return new OffenderAssessmentResource.CreateCategorisationResponse(responseBuilder.build());
        }

        public static OffenderAssessmentResource.CreateCategorisationResponse respond400WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new OffenderAssessmentResource.CreateCategorisationResponse(responseBuilder.build(), entity);
        }

        public static OffenderAssessmentResource.CreateCategorisationResponse respond403WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(403)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new OffenderAssessmentResource.CreateCategorisationResponse(responseBuilder.build(), entity);
        }

        public static OffenderAssessmentResource.CreateCategorisationResponse respond404WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new OffenderAssessmentResource.CreateCategorisationResponse(responseBuilder.build(), entity);
        }

        public static OffenderAssessmentResource.CreateCategorisationResponse respond409WithApplicationJson(ErrorResponse entity) {
            ResponseBuilder responseBuilder = Response.status(409)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new OffenderAssessmentResource.CreateCategorisationResponse(responseBuilder.build(), entity);
        }
    }
}
