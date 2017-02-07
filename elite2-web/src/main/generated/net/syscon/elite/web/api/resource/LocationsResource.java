
package net.syscon.elite.web.api.resource;

import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.Movement;


/**
 * The collection of internal locations within an agency.
 * 
 */
@Path("locations")
public interface LocationsResource {


    /**
     * 
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param orderBy
     *     Order by field: locationId, agencyId, locationType, parentLocationId, livingUnit, or housingUnitType
     *     
     * @param order
     *     Order
     */
    @GET
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsResponse getLocations(
        @QueryParam("orderBy")
        String orderBy,
        @QueryParam("order")
        @DefaultValue("asc")
        LocationsResource.Order order,
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit)
        throws Exception
    ;

    /**
     * 
     * @param locationId
     *     
     */
    @GET
    @Path("locations/{locationId}")
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsLocationsByLocationIdResponse getLocationsLocationsByLocationId(
        @PathParam("locationId")
        String locationId)
        throws Exception
    ;

    /**
     * 
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param locationId
     *     
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param orderBy
     *     Order by field: moveCategory, moveDateTime, fromAgencyId, toAgencyId, moveType, moveReason, fromLocationId, or toLocation
     *     
     * @param order
     *     Order
     */
    @GET
    @Path("locations/{locationId}/movements")
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse getLocationsLocationsByLocationIdMovements(
        @PathParam("locationId")
        String locationId,
        @QueryParam("orderBy")
        String orderBy,
        @QueryParam("order")
        @DefaultValue("asc")
        LocationsResource.Order order,
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit)
        throws Exception
    ;

    /**
     * 
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param locationId
     *     
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param orderBy
     *     Order by field: inmateId, bookingId, offenderId, firstName, lastName, alertCodes, agencyId, currentLocationId, or assignedLivingUnitId
     *     
     * @param order
     *     Order
     */
    @GET
    @Path("locations/{locationId}/inmates")
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse getLocationsLocationsByLocationIdInmates(
        @PathParam("locationId")
        String locationId,
        @QueryParam("orderBy")
        String orderBy,
        @QueryParam("order")
        @DefaultValue("asc")
        LocationsResource.Order order,
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit)
        throws Exception
    ;

    public class GetLocationsLocationsByLocationIdInmatesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsLocationsByLocationIdInmatesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse withJsonOK(List<Movement> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "400",
         * 	"code": "400",
         * 	"message": "Invalid request body",
         * 	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "400",
         *     	"code": "400",
         *     	"message": "Invalid request body",
         *     	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "404",
         * 	"code": "404",
         * 	"message": "Resource not found",
         * 	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "404",
         *     	"code": "404",
         *     	"message": "Resource not found",
         *     	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         *   "httpStatus": "500",
         *   "code": "500",
         *   "message": "Unexpected server error",
         *   "developerMessage": "Unexpected server error",
         *   "moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *       "httpStatus": "500",
         *       "code": "500",
         *       "message": "Unexpected server error",
         *       "developerMessage": "Unexpected server error",
         *       "moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdInmatesResponse(responseBuilder.build());
        }

    }

    public class GetLocationsLocationsByLocationIdMovementsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsLocationsByLocationIdMovementsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse withJsonOK(Movement entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "400",
         * 	"code": "400",
         * 	"message": "Invalid request body",
         * 	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "400",
         *     	"code": "400",
         *     	"message": "Invalid request body",
         *     	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "404",
         * 	"code": "404",
         * 	"message": "Resource not found",
         * 	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "404",
         *     	"code": "404",
         *     	"message": "Resource not found",
         *     	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         *   "httpStatus": "500",
         *   "code": "500",
         *   "message": "Unexpected server error",
         *   "developerMessage": "Unexpected server error",
         *   "moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *       "httpStatus": "500",
         *       "code": "500",
         *       "message": "Unexpected server error",
         *       "developerMessage": "Unexpected server error",
         *       "moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdMovementsResponse(responseBuilder.build());
        }

    }

    public class GetLocationsLocationsByLocationIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsLocationsByLocationIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdResponse withJsonOK(Location entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "400",
         * 	"code": "400",
         * 	"message": "Invalid request body",
         * 	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "400",
         *     	"code": "400",
         *     	"message": "Invalid request body",
         *     	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "404",
         * 	"code": "404",
         * 	"message": "Resource not found",
         * 	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "404",
         *     	"code": "404",
         *     	"message": "Resource not found",
         *     	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         *   "httpStatus": "500",
         *   "code": "500",
         *   "message": "Unexpected server error",
         *   "developerMessage": "Unexpected server error",
         *   "moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *       "httpStatus": "500",
         *       "code": "500",
         *       "message": "Unexpected server error",
         *       "developerMessage": "Unexpected server error",
         *       "moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsLocationsByLocationIdResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsLocationsByLocationIdResponse(responseBuilder.build());
        }

    }

    public class GetLocationsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsResponse withJsonOK(Location entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "400",
         * 	"code": "400",
         * 	"message": "Invalid request body",
         * 	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "400",
         *     	"code": "400",
         *     	"message": "Invalid request body",
         *     	"developerMessage": "The request could not be understood by the server due to malformed syntax. The client should not repeat the request without modifications.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "404",
         * 	"code": "404",
         * 	"message": "Resource not found",
         * 	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "404",
         *     	"code": "404",
         *     	"message": "Resource not found",
         *     	"developerMessage": "The resource could not be found or the user does not have authorization to view the data.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         *   "httpStatus": "500",
         *   "code": "500",
         *   "message": "Unexpected server error",
         *   "developerMessage": "Unexpected server error",
         *   "moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *       "httpStatus": "500",
         *       "code": "500",
         *       "message": "Unexpected server error",
         *       "developerMessage": "Unexpected server error",
         *       "moreInfo": ""
         *     }
         *     
         */
        public static LocationsResource.GetLocationsResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsResponse(responseBuilder.build());
        }

    }

    public enum Order {

        desc,
        asc;

    }

}
