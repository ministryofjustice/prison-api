
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
import net.syscon.elite.web.api.model.InmateDetail;
import net.syscon.elite.web.api.model.Movement;


/**
 * The collection of inmates within the system.
 * 
 */
@Path("booking")
public interface BookingResource {


    /**
     * 
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param orderBy
     *     Order by field: inmateId, bookingId, offenderId, firstName, lastName, alertCodes, agencyId, currentLocationId, or assignedLivingUnitId
     *     
     * @param order
     *     Order
     */
    @GET
    @Produces({
        "application/json"
    })
    BookingResource.GetBookingResponse getBooking(
        @QueryParam("orderBy")
        String orderBy,
        @QueryParam("order")
        @DefaultValue("asc")
        BookingResource.Order order,
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
     * @param bookingId
     *     
     */
    @GET
    @Path("{bookingId}")
    @Produces({
        "application/json"
    })
    BookingResource.GetBookingByBookingIdResponse getBookingByBookingId(
        @PathParam("bookingId")
        String bookingId)
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
     * @param bookingId
     *     
     * @param order
     *     Order
     */
    @GET
    @Path("{bookingId}/movements")
    @Produces({
        "application/json"
    })
    BookingResource.GetBookingByBookingIdMovementsResponse getBookingByBookingIdMovements(
        @PathParam("bookingId")
        String bookingId,
        @QueryParam("orderBy")
        String orderBy,
        @QueryParam("order")
        @DefaultValue("asc")
        BookingResource.Order order,
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit)
        throws Exception
    ;

    public class GetBookingByBookingIdMovementsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetBookingByBookingIdMovementsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static BookingResource.GetBookingByBookingIdMovementsResponse withJsonOK(List<Movement> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdMovementsResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingByBookingIdMovementsResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdMovementsResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingByBookingIdMovementsResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdMovementsResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingByBookingIdMovementsResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdMovementsResponse(responseBuilder.build());
        }

    }

    public class GetBookingByBookingIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetBookingByBookingIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static BookingResource.GetBookingByBookingIdResponse withJsonOK(InmateDetail entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingByBookingIdResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingByBookingIdResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingByBookingIdResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingByBookingIdResponse(responseBuilder.build());
        }

    }

    public class GetBookingResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetBookingResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static BookingResource.GetBookingResponse withJsonOK(List<AssignedInmate> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingResponse(responseBuilder.build());
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
        public static BookingResource.GetBookingResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new BookingResource.GetBookingResponse(responseBuilder.build());
        }

    }

    public enum Order {

        desc,
        asc;

    }

}
