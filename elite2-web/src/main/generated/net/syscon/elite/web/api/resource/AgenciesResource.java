
package net.syscon.elite.web.api.resource;

import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.Location;


/**
 * Returns the list of agencies viewable by the user
 * 
 */
@Path("agencies")
public interface AgenciesResource {


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
    AgenciesResource.GetAgenciesResponse getAgencies(
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
     * @param agencyId
     *     
     */
    @GET
    @Path("agencies/{agencyId}")
    @Produces({
        "application/json"
    })
    AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse getAgenciesAgenciesByAgencyId(
        @PathParam("agencyId")
        String agencyId)
        throws Exception
    ;

    /**
     * 
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param orderBy
     *     Order by field: moveCategory, moveDateTime, fromAgencyId, toAgencyId, moveType, moveReason, fromLocationId, or toLocation
     *     
     * @param agencyId
     *     
     * @param order
     *     Order
     */
    @GET
    @Path("agencies/{agencyId}/locations")
    @Produces({
        "application/json"
    })
    AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse getAgenciesAgenciesByAgencyIdLocations(
        @PathParam("agencyId")
        String agencyId,
        @QueryParam("orderBy")
        String orderBy,
        @QueryParam("order")
        @DefaultValue("asc")
        AgenciesResource.Order order,
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit)
        throws Exception
    ;

    public class GetAgenciesAgenciesByAgencyIdLocationsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetAgenciesAgenciesByAgencyIdLocationsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse withJsonOK(List<Location> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdLocationsResponse(responseBuilder.build());
        }

    }

    public class GetAgenciesAgenciesByAgencyIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetAgenciesAgenciesByAgencyIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse withJsonOK(Agency entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesAgenciesByAgencyIdResponse(responseBuilder.build());
        }

    }

    public class GetAgenciesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetAgenciesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static AgenciesResource.GetAgenciesResponse withJsonOK(List<Agency> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesResponse(responseBuilder.build());
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
        public static AgenciesResource.GetAgenciesResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesResponse(responseBuilder.build());
        }

    }

    public enum Order {

        desc,
        asc;

    }

}
