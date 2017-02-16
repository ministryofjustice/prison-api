
package net.syscon.elite.web.api.resource;

import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.syscon.elite.web.api.model.AssignedInmate;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.Location;
import net.syscon.elite.web.api.model.Movement;


/**
 * Returns the list of locations viewable by the user
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
     */
    @GET
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsResponse getLocations(
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
    @Path("{locationId}")
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsByLocationIdResponse getLocationsByLocationId(
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
    @Path("{locationId}/movements")
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsByLocationIdMovementsResponse getLocationsByLocationIdMovements(
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
    @Path("{locationId}/inmates")
    @Produces({
        "application/json"
    })
    LocationsResource.GetLocationsByLocationIdInmatesResponse getLocationsByLocationIdInmates(
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

    public class GetLocationsByLocationIdInmatesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsByLocationIdInmatesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsByLocationIdInmatesResponse withJsonOK(List<AssignedInmate> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdInmatesResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdInmatesResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdInmatesResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdInmatesResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdInmatesResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdInmatesResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdInmatesResponse(responseBuilder.build());
        }

    }

    public class GetLocationsByLocationIdMovementsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsByLocationIdMovementsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsByLocationIdMovementsResponse withJsonOK(List<Movement> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdMovementsResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdMovementsResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdMovementsResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdMovementsResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdMovementsResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdMovementsResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdMovementsResponse(responseBuilder.build());
        }

    }

    public class GetLocationsByLocationIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetLocationsByLocationIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static LocationsResource.GetLocationsByLocationIdResponse withJsonOK(Location entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsByLocationIdResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new LocationsResource.GetLocationsByLocationIdResponse(responseBuilder.build());
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
        public static LocationsResource.GetLocationsResponse withJsonOK(List<Location> entity) {
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
