package net.syscon.elite.v2.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.v2.api.model.ErrorResponse;
import net.syscon.elite.v2.api.model.OffenderBooking;
import net.syscon.elite.v2.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "Offender Search", produces = MediaType.APPLICATION_JSON)
public interface SearchResource {
    @GET
    @Path("/{locationPrefix: .*}")
    @Produces("application/json")
    @ApiOperation(value = "Returns a list offenders matching locations", nickname = "searchForOffendersLocationOnly")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OffenderBookings successfully retrieved.", response = OffenderBooking.class, responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(name = "Total-Records", description = "Total number of records available", response = Long.class),
                            @ResponseHeader(name = "Page-Offset", description = "Offset of first returned record", response = Long.class),
                            @ResponseHeader(name = "Page-Limit", description = "Limit for number of records returned", response = Long.class)
                    })
    })
    SearchResource.GetSearchResponse searchForOffendersLocationOnly(@ApiParam(value = "Location area to search for offenders") @PathParam("locationPrefix") String locationPrefix,
                                                                    @ApiParam(value = "Comma seperated list of fields to sort") @HeaderParam("Sort-Fields") String sortFields,
                                                                    @ApiParam(value = "Sort Order asc or desc") @HeaderParam("Sort-Order") String sortOrder,
                                                                    @ApiParam(value = "Offset of first returned record") @HeaderParam("Page-Offset") Long offset,
                                                                    @ApiParam(value = "Limit for number of records returned") @HeaderParam("Page-Limit") Long limit);

    @GET
    @Path("/{locationPrefix: .*}/{keywords: .*}")
    @Produces("application/json")
    @ApiOperation(value = "Returns a list offenders matching keywords and locations", nickname = "searchForOffendersLocationAndKeyword")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OffenderBookings successfully retrieved.", response = OffenderBooking.class, responseContainer = "List",
                    responseHeaders = {
                            @ResponseHeader(name = "Total-Records", description = "Total number of records available", response = Long.class),
                            @ResponseHeader(name = "Page-Offset", description = "Offset of first returned record", response = Long.class),
                            @ResponseHeader(name = "Page-Limit", description = "Limit for number of records returned", response = Long.class)
                    })
    })
    SearchResource.GetSearchResponse searchForOffendersLocationAndKeyword(@ApiParam(value = "Location area to search for offenders") @PathParam("locationPrefix") String locationPrefix,
                                                                          @ApiParam(value = "Keywords to search for offenders") @PathParam("keywords") String keywords,
                                                                          @ApiParam(value = "Comma seperated list of fields to sort") @HeaderParam("Sort-Fields") String sortFields,
                                                                          @ApiParam(value = "Sort Order asc or desc") @HeaderParam("Sort-Order") String sortOrder,
                                                                          @ApiParam(value = "Offset of first returned record") @HeaderParam("Page-Offset") Long offset,
                                                                          @ApiParam(value = "Limit for number of records returned") @HeaderParam("Page-Limit") Long limit);


    class GetSearchResponse extends ResponseDelegate {
        private GetSearchResponse(Response response, Object entity) {
            super(response, entity);
        }

        private GetSearchResponse(Response response) {
            super(response);
        }

        public static GetSearchResponse respond200WithApplicationJson(List<OffenderBooking> entity, Long offset, Long limit, Long totalRecords) {
            Response.ResponseBuilder responseBuilder = Response.status(200)
                    .header("Content-Type", "application/json")
                    .header("Total-Records", totalRecords)
                    .header("Page-Offset", offset)
                    .header("Page-Limit", limit);
            responseBuilder.entity(entity);
            return new GetSearchResponse(responseBuilder.build(), entity);
        }

        public static GetSearchResponse respond400WithApplicationJson(ErrorResponse entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetSearchResponse(responseBuilder.build(), entity);
        }

        public static GetSearchResponse respond404WithApplicationJson(ErrorResponse entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetSearchResponse(responseBuilder.build(), entity);
        }

        public static GetSearchResponse respond500WithApplicationJson(ErrorResponse entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new GetSearchResponse(responseBuilder.build(), entity);
        }
    }
}
