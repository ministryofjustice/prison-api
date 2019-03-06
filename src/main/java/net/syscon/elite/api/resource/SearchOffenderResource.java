package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.OffenderBooking;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = {"/search-offenders"})
@SuppressWarnings("unused")
public interface SearchOffenderResource {

    @GET
    @Path("/{locationPrefix}/{keywords}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Deprecated
    @ApiOperation(value = "List offenders by location (matching keywords).", notes = "Deprecated: Use <b>/locations/description/{locationPrefix}/inmates</b> instead. This API will be removed in a future release.", nickname="searchForOffendersLocationAndKeyword")
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "OK", response = OffenderBooking.class, responseContainer = "List"),
        @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
        @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List") })
    SearchForOffendersLocationAndKeywordResponse searchForOffendersLocationAndKeyword(@ApiParam(value = "", required = true) @PathParam("locationPrefix") String locationPrefix,
                                                                                      @ApiParam(value = "", required = true) @PathParam("keywords") String keywords,
                                                                                      @ApiParam(value = "return IEP data", defaultValue = "false") @QueryParam("returnIep") boolean returnIep,
                                                                                      @ApiParam(value = "return Alert data", defaultValue = "false") @QueryParam("returnAlerts") boolean returnAlerts,
                                                                                      @ApiParam(value = "Requested offset of first record in returned collection of search-offender records.", defaultValue = "0") @HeaderParam("Page-Offset") Long pageOffset,
                                                                                      @ApiParam(value = "Requested limit to number of search-offender records returned.", defaultValue = "10") @HeaderParam("Page-Limit") Long pageLimit,
                                                                                      @ApiParam(value = "Comma separated list of one or more of the following fields - <b><<fieldsList>></b>") @HeaderParam("Sort-Fields") String sortFields,
                                                                                      @ApiParam(value = "Sort order (ASC or DESC) - defaults to ASC.", defaultValue = "ASC") @HeaderParam("Sort-Order") Order sortOrder);

    class SearchForOffendersLocationAndKeywordResponse extends ResponseDelegate {

        private SearchForOffendersLocationAndKeywordResponse(final Response response) {
            super(response);
        }

        private SearchForOffendersLocationAndKeywordResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static SearchForOffendersLocationAndKeywordResponse respond200WithApplicationJson(final Page<OffenderBooking> page) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON)
                    .header("Total-Records", page.getTotalRecords())
                    .header("Page-Offset", page.getPageOffset())
                    .header("Page-Limit", page.getPageLimit());
            responseBuilder.entity(page.getItems());
            return new SearchForOffendersLocationAndKeywordResponse(responseBuilder.build(), page.getItems());
        }

        public static SearchForOffendersLocationAndKeywordResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new SearchForOffendersLocationAndKeywordResponse(responseBuilder.build(), entity);
        }

        public static SearchForOffendersLocationAndKeywordResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new SearchForOffendersLocationAndKeywordResponse(responseBuilder.build(), entity);
        }

        public static SearchForOffendersLocationAndKeywordResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new SearchForOffendersLocationAndKeywordResponse(responseBuilder.build(), entity);
        }
    }
}
