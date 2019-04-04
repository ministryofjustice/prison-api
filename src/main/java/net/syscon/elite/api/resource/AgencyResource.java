package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.ResponseDelegate;
import net.syscon.elite.api.support.TimeSlot;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/agencies"})
@SuppressWarnings("unused")
public interface AgencyResource {

    @GET
    @Path("/")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of active agencies.", notes = "List of active agencies.", nickname="getAgencies")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = Agency.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetAgenciesResponse getAgencies(@ApiParam(value = "Requested offset of first record in returned collection of agency records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                    @ApiParam(value = "Requested limit to number of agency records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit);

    @GET
    @Path("/{agencyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Agency detail.", notes = "Agency detail.", nickname="getAgency")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Agency.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetAgencyResponse getAgency(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId);

    @GET
    @Path("/{agencyId}/eventLocations")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of locations for agency where events (appointments, visits, activities) could be held.", notes = "List of locations for agency where events (appointments, visits, activities) could be held.", nickname="getAgencyEventLocations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetAgencyEventLocationsResponse getAgencyEventLocations(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                                            @ApiParam(value = "Comma separated list of one or more of the following fields - <b>description, userDescription</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                            @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{agencyId}/eventLocationsBooked")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of locations for agency where events (appointments, visits, activities) are being held.", notes = "List of locations for agency where events (appointments, visits, activities) are being held.", nickname="getAgencyEventLocationsBooked")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetAgencyEventLocationsBookedResponse getAgencyEventLocationsBooked(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                                                        @ApiParam(value = "Filter list to only return locations which prisoners will be attending on this day", required = true) @QueryParam("bookedOnDay") LocalDate bookedOnDay,
                                                                        @ApiParam(value = "Only return locations which prisoners will be attending in this time slot (AM, PM or ED, and bookedOnDay must be specified)") @QueryParam("timeSlot") TimeSlot timeSlot);

    @GET
    @Path("/{agencyId}/locations")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of active internal locations for agency.", notes = "List of active internal locations for agency.", nickname="getAgencyLocations")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetAgencyLocationsResponse getAgencyLocations(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId,
                                                  @ApiParam(value = "Restricts list of locations returned to those that can be used for the specified event type.") @QueryParam("eventType") String eventType,
                                                  @ApiParam(value = "Comma separated list of one or more of the following fields - <b>description, userDescription</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                  @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/{agencyId}/locations/groups")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of all available Location Groups at agency.", notes = "List of all available Location Groups at agency.", nickname="getAvailableLocationGroups")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = LocationGroup.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetAvailableLocationGroupsResponse getAvailableLocationGroups(@ApiParam(value = "The prison", required = true) @PathParam("agencyId") String agencyId);

    @GET
    @Path("/{agencyId}/locations/whereabouts")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Whereabouts details (e.g. whether enabled) for prison.", notes = "Whereabouts details (e.g. whether enabled) for prison.", nickname="getWhereabouts")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = WhereaboutsConfig.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    WhereaboutsConfig getWhereabouts(@ApiParam(value = "The prison", required = true) @PathParam("agencyId") String agencyId);

    @GET
    @Path("/caseload/{caseload}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of agencies for caseload.", notes = "List of agencies for caseload.", nickname="getAgenciesByCaseload")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Agency.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetAgenciesByCaseloadResponse getAgenciesByCaseload(@ApiParam(value = "", required = true) @PathParam("caseload") String caseload);

    @GET
    @Path("/prison")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of prison contact details.", notes = "List of prison contact details.", nickname="getPrisonContactDetailList")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PrisonContactDetail.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetPrisonContactDetailListResponse getPrisonContactDetailList();

    @GET
    @Path("/prison/{agencyId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Prison contact detail.", notes = "Prison contact detail.", nickname="getPrisonContactDetail")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = PrisonContactDetail.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetPrisonContactDetailResponse getPrisonContactDetail(@ApiParam(value = "", required = true) @PathParam("agencyId") String agencyId);

    class GetAgenciesResponse extends ResponseDelegate {

        private GetAgenciesResponse(final Response response) {
            super(response);
        }

        private GetAgenciesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAgenciesResponse respond200WithApplicationJson(final Page<Agency> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetAgenciesResponse(responseBuilder.build(), page.getItems());
        }

        public static GetAgenciesResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesResponse(responseBuilder.build(), entity);
        }

        public static GetAgenciesResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesResponse(responseBuilder.build(), entity);
        }

        public static GetAgenciesResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesResponse(responseBuilder.build(), entity);
        }
    }

    class GetAgencyResponse extends ResponseDelegate {

        private GetAgencyResponse(final Response response) {
            super(response);
        }

        private GetAgencyResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAgencyResponse respond200WithApplicationJson(final Agency entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyResponse(responseBuilder.build(), entity);
        }
    }

    class GetAgencyEventLocationsResponse extends ResponseDelegate {

        private GetAgencyEventLocationsResponse(final Response response) {
            super(response);
        }

        private GetAgencyEventLocationsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAgencyEventLocationsResponse respond200WithApplicationJson(final List<Location> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyEventLocationsResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyEventLocationsResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyEventLocationsResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsResponse(responseBuilder.build(), entity);
        }
    }

    class GetAgencyEventLocationsBookedResponse extends ResponseDelegate {

        private GetAgencyEventLocationsBookedResponse(final Response response) {
            super(response);
        }

        private GetAgencyEventLocationsBookedResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAgencyEventLocationsBookedResponse respond200WithApplicationJson(final List<Location> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsBookedResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyEventLocationsBookedResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsBookedResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyEventLocationsBookedResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsBookedResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyEventLocationsBookedResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyEventLocationsBookedResponse(responseBuilder.build(), entity);
        }
    }

    class GetAgencyLocationsResponse extends ResponseDelegate {

        private GetAgencyLocationsResponse(final Response response) {
            super(response);
        }

        private GetAgencyLocationsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAgencyLocationsResponse respond200WithApplicationJson(final List<Location> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyLocationsResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyLocationsResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
        }

        public static GetAgencyLocationsResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
        }
    }

    class GetAvailableLocationGroupsResponse extends ResponseDelegate {

        private GetAvailableLocationGroupsResponse(final Response response) {
            super(response);
        }

        private GetAvailableLocationGroupsResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAvailableLocationGroupsResponse respond200WithApplicationJson(final List<LocationGroup> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableLocationGroupsResponse(responseBuilder.build(), entity);
        }

        public static GetAvailableLocationGroupsResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableLocationGroupsResponse(responseBuilder.build(), entity);
        }

        public static GetAvailableLocationGroupsResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableLocationGroupsResponse(responseBuilder.build(), entity);
        }

        public static GetAvailableLocationGroupsResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAvailableLocationGroupsResponse(responseBuilder.build(), entity);
        }
    }

    class GetAgenciesByCaseloadResponse extends ResponseDelegate {

        private GetAgenciesByCaseloadResponse(final Response response) {
            super(response);
        }

        private GetAgenciesByCaseloadResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAgenciesByCaseloadResponse respond200WithApplicationJson(final List<Agency> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesByCaseloadResponse(responseBuilder.build(), entity);
        }

        public static GetAgenciesByCaseloadResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesByCaseloadResponse(responseBuilder.build(), entity);
        }

        public static GetAgenciesByCaseloadResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesByCaseloadResponse(responseBuilder.build(), entity);
        }

        public static GetAgenciesByCaseloadResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAgenciesByCaseloadResponse(responseBuilder.build(), entity);
        }
    }

    class GetPrisonContactDetailListResponse extends ResponseDelegate {

        private GetPrisonContactDetailListResponse(final Response response) {
            super(response);
        }

        private GetPrisonContactDetailListResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetPrisonContactDetailListResponse respond200WithApplicationJson(final List<PrisonContactDetail> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailListResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonContactDetailListResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailListResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonContactDetailListResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailListResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonContactDetailListResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailListResponse(responseBuilder.build(), entity);
        }
    }

    class GetPrisonContactDetailResponse extends ResponseDelegate {

        private GetPrisonContactDetailResponse(final Response response) {
            super(response);
        }

        private GetPrisonContactDetailResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetPrisonContactDetailResponse respond200WithApplicationJson(final PrisonContactDetail entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonContactDetailResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonContactDetailResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailResponse(responseBuilder.build(), entity);
        }

        public static GetPrisonContactDetailResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetPrisonContactDetailResponse(responseBuilder.build(), entity);
        }
    }
}
