
package net.syscon.elite.web.api.resource;

import java.util.List;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.syscon.elite.web.api.model.HttpStatus;
import net.syscon.elite.web.api.model.InmateSummary;


/**
 * Returns the list of agencies viewable by the user
 * 
 */
@Path("inmates")
public interface InmatesResource {


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
    InmatesResource.GetInmatesResponse getInmates(
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit)
        throws Exception
    ;

    /**
     * Returns the within the system.
     * 
     */
    @GET
    @Path("inmates")
    InmatesResource.GetInmatesInmatesResponse getInmatesInmates()
        throws Exception
    ;

    public class GetInmatesInmatesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetInmatesInmatesResponse(Response delegate) {
            super(delegate);
        }

    }

    public class GetInmatesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetInmatesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static InmatesResource.GetInmatesResponse withJsonOK(List<InmateSummary> entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new InmatesResource.GetInmatesResponse(responseBuilder.build());
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
        public static InmatesResource.GetInmatesResponse withJsonBadRequest(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(400).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new InmatesResource.GetInmatesResponse(responseBuilder.build());
        }

        /**
         *  e.g. {
         * 	"httpStatus": "404",
         * 	"code": "404",
         * 	"message": "Resource not found",
         * 	"developerMessage": "The resource could not be found. The client should not repeat the request without modifications.",
         * 	"moreInfo": ""
         * }
         * 
         * 
         * @param entity
         *     {
         *     	"httpStatus": "404",
         *     	"code": "404",
         *     	"message": "Resource not found",
         *     	"developerMessage": "The resource could not be found. The client should not repeat the request without modifications.",
         *     	"moreInfo": ""
         *     }
         *     
         */
        public static InmatesResource.GetInmatesResponse withJsonNotFound(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(404).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new InmatesResource.GetInmatesResponse(responseBuilder.build());
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
        public static InmatesResource.GetInmatesResponse withJsonInternalServerError(HttpStatus entity) {
            Response.ResponseBuilder responseBuilder = Response.status(500).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new InmatesResource.GetInmatesResponse(responseBuilder.build());
        }

    }

}
