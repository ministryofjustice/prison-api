
package net.syscon.elite.web.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.syscon.elite.web.api.model.Agencies;
import net.syscon.elite.web.api.model.Agency;

@Path("agencies")
public interface AgenciesResource {


    /**
     * 
     * @param authorization
     *     The auth token for this request
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
        @HeaderParam("Authorization")
        String authorization,
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
     * @param authorization
     *     The auth token for this request
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param entity
     *     
     */
    @POST
    @Consumes("application/json")
    @Produces({
        "application/json"
    })
    AgenciesResource.PostAgenciesResponse postAgencies(
        @HeaderParam("Authorization")
        String authorization,
        @QueryParam("offset")
        @DefaultValue("0")
        int offset,
        @QueryParam("limit")
        @DefaultValue("10")
        int limit, Agency entity)
        throws Exception
    ;

    public class GetAgenciesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetAgenciesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * Unauthorized
         * 
         */
        public static AgenciesResource.GetAgenciesResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new AgenciesResource.GetAgenciesResponse(responseBuilder.build());
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static AgenciesResource.GetAgenciesResponse withJsonOK(Agencies entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.GetAgenciesResponse(responseBuilder.build());
        }

    }

    public class PostAgenciesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private PostAgenciesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * Created
         * 
         * @param entity
         *     
         */
        public static AgenciesResource.PostAgenciesResponse withJsonCreated(Agency entity) {
            Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new AgenciesResource.PostAgenciesResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static AgenciesResource.PostAgenciesResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new AgenciesResource.PostAgenciesResponse(responseBuilder.build());
        }

    }

}
