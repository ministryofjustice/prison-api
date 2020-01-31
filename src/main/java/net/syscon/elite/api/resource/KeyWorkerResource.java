package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.KeyWorkerAllocationDetail;
import net.syscon.elite.api.model.Keyworker;
import net.syscon.elite.api.model.OffenderKeyWorker;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/key-worker"})
@SuppressWarnings("unused")
public interface KeyWorkerResource {

    @GET
    @Path("/{agencyId}/allocationHistory")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "All allocations in specified agency.", notes = "All allocations in specified agency.", nickname = "getAllocationHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = OffenderKeyWorker.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetAllocationHistoryResponse getAllocationHistory(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathParam("agencyId") String agencyId,
                                                      @ApiParam(value = "Requested offset of first record in returned collection of allocationHistory records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                      @ApiParam(value = "Requested limit to number of allocationHistory records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit);

    @GET
    @Path("/{agencyId}/available")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Key workers available for allocation at specified agency.", notes = "Key workers available for allocation at specified agency.", nickname = "getAvailableKeyworkers")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = Keyworker.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetAvailableKeyworkersResponse getAvailableKeyworkers(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathParam("agencyId") String agencyId);

    @GET
    @Path("/{staffId}/agency/{agencyId}/offenders")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Specified key worker's currently assigned offenders.", notes = "Specified key worker's currently assigned offenders.", nickname = "getAllocationsForKeyworker")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = KeyWorkerAllocationDetail.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetAllocationsForKeyworkerResponse getAllocationsForKeyworker(@ApiParam(value = "The key worker staff id", required = true) @PathParam("staffId") Long staffId,
                                                                  @ApiParam(value = "The agency (prison) identifier.", required = true) @PathParam("agencyId") String agencyId);

    @POST
    @Path("/{agencyId}/current-allocations")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", nickname = "postKeyWorkerAgencyIdCurrentAllocations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations list is returned.", response = KeyWorkerAllocationDetail.class, responseContainer = "List")})
    PostKeyWorkerAgencyIdCurrentAllocationsResponse postKeyWorkerAgencyIdCurrentAllocations(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathParam("agencyId") String agencyId,
                                                                                            @ApiParam(value = "The required staff Ids (mandatory)", required = true) List<Long> body);

    @POST
    @Path("/{agencyId}/current-allocations/offenders")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently assigned offenders - POST version to allow larger staff lists.", nickname = "postKeyWorkerAgencyIdCurrentAllocationsOffenders")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations list is returned.", response = KeyWorkerAllocationDetail.class, responseContainer = "List")})
    PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse postKeyWorkerAgencyIdCurrentAllocationsOffenders(@ApiParam(value = "The agency (prison) identifier.", required = true) @PathParam("agencyId") String agencyId,
                                                                                                              @ApiParam(value = "The required offender Nos (mandatory)", required = true) List<String> body);

    @POST
    @Path("/offenders/allocationHistory")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", notes = "Retrieves Specified prisoners allocation history - POST version to allow larger allocation lists.", nickname = "postKeyWorkerOffendersAllocationHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations history list is returned.", response = OffenderKeyWorker.class, responseContainer = "List")})
    PostKeyWorkerOffendersAllocationHistoryResponse postKeyWorkerOffendersAllocationHistory(@ApiParam(value = "The required offender nos (mandatory)", required = true) List<String> body);

    @POST
    @Path("/staff/allocationHistory")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", notes = "Retrieves Specified key worker's currently allocation history - POST version to allow larger staff lists.", nickname = "postKeyWorkerStaffAllocationHistory")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "The allocations history list is returned.", response = OffenderKeyWorker.class, responseContainer = "List")})
    PostKeyWorkerStaffAllocationHistoryResponse postKeyWorkerStaffAllocationHistory(@ApiParam(value = "The required staff Ids (mandatory)", required = true) List<Long> body);

    class GetAllocationHistoryResponse extends ResponseDelegate {

        private GetAllocationHistoryResponse(final Response response) {
            super(response);
        }

        private GetAllocationHistoryResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAllocationHistoryResponse respond200WithApplicationJson(final Page<OffenderKeyWorker> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetAllocationHistoryResponse(responseBuilder.build(), page.getItems());
        }

        public static GetAllocationHistoryResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationHistoryResponse(responseBuilder.build(), entity);
        }

        public static GetAllocationHistoryResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationHistoryResponse(responseBuilder.build(), entity);
        }

        public static GetAllocationHistoryResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationHistoryResponse(responseBuilder.build(), entity);
        }
    }

    class GetAvailableKeyworkersResponse extends ResponseDelegate {

        private GetAvailableKeyworkersResponse(final Response response) {
            super(response);
        }

        private GetAvailableKeyworkersResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAvailableKeyworkersResponse respond200WithApplicationJson(final List<Keyworker> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableKeyworkersResponse(responseBuilder.build(), entity);
        }

        public static GetAvailableKeyworkersResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableKeyworkersResponse(responseBuilder.build(), entity);
        }

        public static GetAvailableKeyworkersResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableKeyworkersResponse(responseBuilder.build(), entity);
        }

        public static GetAvailableKeyworkersResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableKeyworkersResponse(responseBuilder.build(), entity);
        }
    }

    class GetAllocationsForKeyworkerResponse extends ResponseDelegate {

        private GetAllocationsForKeyworkerResponse(final Response response) {
            super(response);
        }

        private GetAllocationsForKeyworkerResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAllocationsForKeyworkerResponse respond200WithApplicationJson(final List<KeyWorkerAllocationDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationsForKeyworkerResponse(responseBuilder.build(), entity);
        }

        public static GetAllocationsForKeyworkerResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationsForKeyworkerResponse(responseBuilder.build(), entity);
        }

        public static GetAllocationsForKeyworkerResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationsForKeyworkerResponse(responseBuilder.build(), entity);
        }

        public static GetAllocationsForKeyworkerResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAllocationsForKeyworkerResponse(responseBuilder.build(), entity);
        }
    }

    class PostKeyWorkerAgencyIdCurrentAllocationsResponse extends ResponseDelegate {

        private PostKeyWorkerAgencyIdCurrentAllocationsResponse(final Response response) {
            super(response);
        }

        private PostKeyWorkerAgencyIdCurrentAllocationsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostKeyWorkerAgencyIdCurrentAllocationsResponse respond200WithApplicationJson(final List<KeyWorkerAllocationDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostKeyWorkerAgencyIdCurrentAllocationsResponse(responseBuilder.build(), entity);
        }
    }

    class PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse extends ResponseDelegate {

        private PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse(final Response response) {
            super(response);
        }

        private PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse respond200WithApplicationJson(final List<KeyWorkerAllocationDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostKeyWorkerAgencyIdCurrentAllocationsOffendersResponse(responseBuilder.build(), entity);
        }
    }

    class PostKeyWorkerOffendersAllocationHistoryResponse extends ResponseDelegate {

        private PostKeyWorkerOffendersAllocationHistoryResponse(final Response response) {
            super(response);
        }

        private PostKeyWorkerOffendersAllocationHistoryResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostKeyWorkerOffendersAllocationHistoryResponse respond200WithApplicationJson(final List<OffenderKeyWorker> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostKeyWorkerOffendersAllocationHistoryResponse(responseBuilder.build(), entity);
        }
    }

    class PostKeyWorkerStaffAllocationHistoryResponse extends ResponseDelegate {

        private PostKeyWorkerStaffAllocationHistoryResponse(final Response response) {
            super(response);
        }

        private PostKeyWorkerStaffAllocationHistoryResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static PostKeyWorkerStaffAllocationHistoryResponse respond200WithApplicationJson(final List<OffenderKeyWorker> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new PostKeyWorkerStaffAllocationHistoryResponse(responseBuilder.build(), entity);
        }
    }
}
