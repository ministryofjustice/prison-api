package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Api(tags = {"/locations"})
@SuppressWarnings("unused")
public interface LocationResource {

    @GET
    @Path("/description/{locationPrefix}/inmates")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of offenders at location.", notes = "List of offenders at location.", nickname="getOffendersAtLocationDescription")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetOffendersAtLocationDescriptionResponse getOffendersAtLocationDescription(@ApiParam(value = "", required = true) @PathParam("locationPrefix") String locationPrefix,
                                                                                @ApiParam(value = "offender name or id to match") @QueryParam("keywords") String keywords,
                                                                                @ApiParam(value = "Offenders with a DOB >= this date", example = "1970-01-02") @QueryParam("fromDob") LocalDate fromDob,
                                                                                @ApiParam(value = "Offenders with a DOB <= this date", example = "1975-01-02") @QueryParam("toDob") LocalDate toDob,
                                                                                @ApiParam(value = "alert flags to filter by") @QueryParam("alerts") List<String> alerts,
                                                                                @ApiParam(value = "return IEP data", defaultValue = "false") @QueryParam("returnIep") boolean returnIep,
                                                                                @ApiParam(value = "return Alert data", defaultValue = "false") @QueryParam("returnAlerts") boolean returnAlerts,
                                                                                @ApiParam(value = "retrieve category classification from assessments", defaultValue = "false") @QueryParam("returnCategory") boolean returnCategory,
                                                                                @ApiParam(value = "retrieve inmates with a specific convicted status (Convicted, Remand, default: All)", defaultValue = "All") @QueryParam("convictedStatus") String convictedStatus,
                                                                                @ApiParam(value = "Requested offset of first record in returned collection of inmate records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                                                @ApiParam(value = "Requested limit to number of inmate records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                                                @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                                @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    @GET
    @Path("/groups/{agencyId}/{name}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of cell locations by group at agency location.", notes = "List of cell locations by group at agency location.", nickname="getLocationGroup")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Location.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetLocationGroupResponse getLocationGroup(@ApiParam(value = "The prison", required = true) @PathParam("agencyId") String agencyId,
                                              @ApiParam(value = "The group name", required = true) @PathParam("name") String name);

    @GET
    @Path("/{locationId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Location detail.", notes = "Location detail.", nickname="getLocation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = Location.class),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class) })
    GetLocationResponse getLocation(@ApiParam(value = "The location id of location", required = true) @PathParam("locationId") Long locationId);

    @GET
    @Path("/{locationId}/inmates")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "List of offenders at location.", notes = "List of offenders at location.", nickname="getOffendersAtLocation")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    GetOffendersAtLocationResponse getOffendersAtLocation(@ApiParam(value = "The location id of location", required = true) @PathParam("locationId") Long locationId,
                                                          @ApiParam(value = "Search parameters with the format [connector]:&lt;fieldName&gt;:&lt;operator&gt;:&lt;value&gt;:[format],... <p>Connector operators - and, or <p>Supported Operators - eq, neq, gt, gteq, lt, lteq, like, in</p> <p>Supported Fields - bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</p> ", required = true) @QueryParam("query") String query,
                                                          @ApiParam(value = "Requested offset of first record in returned collection of inmate records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                          @ApiParam(value = "Requested limit to number of inmate records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                          @ApiParam(value = "Comma separated list of one or more of the following fields - <b>bookingNo, bookingId, offenderNo, firstName, lastName, agencyId, or assignedLivingUnitId</b>") @HeaderParam("Sort-Fields") String sortFields,
                                                          @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    class GetOffendersAtLocationDescriptionResponse extends ResponseDelegate {

        private GetOffendersAtLocationDescriptionResponse(final Response response) {
            super(response);
        }

        private GetOffendersAtLocationDescriptionResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffendersAtLocationDescriptionResponse respond200WithApplicationJson(final Page<OffenderBooking> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetOffendersAtLocationDescriptionResponse(responseBuilder.build(), page.getItems());
        }

        public static GetOffendersAtLocationDescriptionResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffendersAtLocationDescriptionResponse(responseBuilder.build(), entity);
        }

        public static GetOffendersAtLocationDescriptionResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffendersAtLocationDescriptionResponse(responseBuilder.build(), entity);
        }

        public static GetOffendersAtLocationDescriptionResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffendersAtLocationDescriptionResponse(responseBuilder.build(), entity);
        }
    }

    class GetLocationGroupResponse extends ResponseDelegate {

        private GetLocationGroupResponse(final Response response) {
            super(response);
        }

        private GetLocationGroupResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetLocationGroupResponse respond200WithApplicationJson(final List<Location> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationGroupResponse(responseBuilder.build(), entity);
        }

        public static GetLocationGroupResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationGroupResponse(responseBuilder.build(), entity);
        }

        public static GetLocationGroupResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationGroupResponse(responseBuilder.build(), entity);
        }

        public static GetLocationGroupResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationGroupResponse(responseBuilder.build(), entity);
        }
    }

    class GetLocationResponse extends ResponseDelegate {

        private GetLocationResponse(final Response response) {
            super(response);
        }

        private GetLocationResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetLocationResponse respond200WithApplicationJson(final Location entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationResponse(responseBuilder.build(), entity);
        }

        public static GetLocationResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationResponse(responseBuilder.build(), entity);
        }

        public static GetLocationResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationResponse(responseBuilder.build(), entity);
        }

        public static GetLocationResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetLocationResponse(responseBuilder.build(), entity);
        }
    }

    class GetOffendersAtLocationResponse extends ResponseDelegate {

        private GetOffendersAtLocationResponse(final Response response) {
            super(response);
        }

        private GetOffendersAtLocationResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetOffendersAtLocationResponse respond200WithApplicationJson(final Page<OffenderBooking> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new GetOffendersAtLocationResponse(responseBuilder.build(), page.getItems());
        }

        public static GetOffendersAtLocationResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffendersAtLocationResponse(responseBuilder.build(), entity);
        }

        public static GetOffendersAtLocationResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffendersAtLocationResponse(responseBuilder.build(), entity);
        }

        public static GetOffendersAtLocationResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetOffendersAtLocationResponse(responseBuilder.build(), entity);
        }
    }
}
