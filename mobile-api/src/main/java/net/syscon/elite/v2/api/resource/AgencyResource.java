package net.syscon.elite.v2.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.v2.api.model.Agency;
import net.syscon.elite.v2.api.model.ErrorResponse;
import net.syscon.elite.v2.api.model.Location;
import net.syscon.elite.v2.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(value = "Agency Resource", produces = MediaType.APPLICATION_JSON)
@Path("/v2/agencies")
public interface AgencyResource {
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets list of agencies applicable for user.", nickname = "getAgencies")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Agencies successfully retrieved.", response = Agency.class, responseContainer = "List",
                  responseHeaders = @ResponseHeader(name = "Total-Records", description = "Total number of records available", response = Long.class))
  })
  GetAgenciesResponse getAgencies(@ApiParam(value = "Pagination offset") @HeaderParam("Page-Offset") Long offset,
                                  @ApiParam(value = "Pagination limit") @HeaderParam("Page-Limit") Long limit);

  @GET
  @Path("/{agencyId}")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Gets agency details for specified agency id.", nickname = "getAgency")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Requested agency details successfully retrieved.", response = Agency.class),
          @ApiResponse(code = 404, message = "Requested agency not found.", response = ErrorResponse.class)
  })
  GetAgencyResponse getAgency(@ApiParam(required = true) @PathParam("agencyId") String agencyId);

  @GET
  @Path("/{agencyId}/locations")
  @Produces(MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Get locations for specified agency.", nickname = "getAgencyLocations")
  @ApiResponses(value = {
          @ApiResponse(code = 200, message = "Locations successfully retrieved for requested agency.", response = Location.class, responseContainer = "List",
                  responseHeaders = @ResponseHeader(name = "Total-Records", description = "Total number of records available", response = Long.class)),
          @ApiResponse(code = 404, message = "Requested agency not found.", response = ErrorResponse.class)
  })
  GetAgencyLocationsResponse getAgencyLocations(@ApiParam(required = true) @PathParam("agencyId") String agencyId,
                                                @ApiParam(value = "Pagination offset") @HeaderParam("Page-Offset") Long offset,
                                                @ApiParam(value = "Pagination limit") @HeaderParam("Page-Limit") Long limit);

  class GetAgenciesResponse extends ResponseDelegate {
    private GetAgenciesResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetAgenciesResponse(Response response) {
      super(response);
    }

    public static GetAgenciesResponse respond200WithApplicationJson(List<Agency> entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgenciesResponse(responseBuilder.build(), entity);
    }

    public static GetAgenciesResponse respond400WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgenciesResponse(responseBuilder.build(), entity);
    }

    public static GetAgenciesResponse respond404WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgenciesResponse(responseBuilder.build(), entity);
    }

    public static GetAgenciesResponse respond500WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgenciesResponse(responseBuilder.build(), entity);
    }
  }

  class GetAgencyResponse extends ResponseDelegate {
    private GetAgencyResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetAgencyResponse(Response response) {
      super(response);
    }

    public static GetAgencyResponse respond200WithApplicationJson(Agency entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyResponse(responseBuilder.build(), entity);
    }

    public static GetAgencyResponse respond400WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyResponse(responseBuilder.build(), entity);
    }

    public static GetAgencyResponse respond404WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyResponse(responseBuilder.build(), entity);
    }

    public static GetAgencyResponse respond500WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyResponse(responseBuilder.build(), entity);
    }
  }

  class GetAgencyLocationsResponse extends ResponseDelegate {
    private GetAgencyLocationsResponse(Response response, Object entity) {
      super(response, entity);
    }

    private GetAgencyLocationsResponse(Response response) {
      super(response);
    }

    public static GetAgencyLocationsResponse respond200WithApplicationJson(List<Location> entity) {
      Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
    }

    public static GetAgencyLocationsResponse respond400WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
    }

    public static GetAgencyLocationsResponse respond404WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
    }

    public static GetAgencyLocationsResponse respond500WithApplicationJson(ErrorResponse entity) {
      Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", MediaType.APPLICATION_JSON);
      responseBuilder.entity(entity);
      return new GetAgencyLocationsResponse(responseBuilder.build(), entity);
    }
  }
}
