
package net.syscon.elite.web.api.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import net.syscon.elite.web.api.model.Agencies;
import net.syscon.elite.web.api.model.Agency;
import net.syscon.elite.web.api.model.Product;
import net.syscon.elite.web.api.model.Products;
import net.syscon.elite.web.api.resource.support.PATCH;

@Path("products")
public interface ProductsResource {


    /**
     * 
     * @param authorization
     *     The auth token for this request
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param region
     *     Filter by region
     */
    @GET
    @Produces({
        "application/json"
    })
    ProductsResource.GetProductsResponse getProducts(
        @HeaderParam("Authorization")
        String authorization,
        @QueryParam("region")
        String region,
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
     * @param entity
     *     
     */
    @POST
    @Consumes("application/json")
    @Produces({
        "application/json"
    })
    ProductsResource.PostProductsResponse postProducts(
        @HeaderParam("Authorization")
        String authorization, Product entity)
        throws Exception
    ;

    /**
     * 
     * @param authorization
     *     The auth token for this request
     * @param productId
     *     
     */
    @GET
    @Path("{productId}")
    @Produces({
        "application/json"
    })
    ProductsResource.GetProductsByProductIdResponse getProductsByProductId(
        @PathParam("productId")
        String productId,
        @HeaderParam("Authorization")
        String authorization)
        throws Exception
    ;

    /**
     * 
     * @param authorization
     *     The auth token for this request
     * @param productId
     *     
     * @param entity
     *     
     */
    @PUT
    @Path("{productId}")
    @Consumes("application/json")
    @Produces({
        "application/json"
    })
    ProductsResource.PutProductsByProductIdResponse putProductsByProductId(
        @PathParam("productId")
        String productId,
        @HeaderParam("Authorization")
        String authorization, Product entity)
        throws Exception
    ;

    /**
     * 
     * @param authorization
     *     The auth token for this request
     * @param productId
     *     
     * @param entity
     *     
     */
    @PATCH
    @Path("{productId}")
    @Consumes("application/json")
    @Produces({
        "application/json"
    })
    ProductsResource.PatchProductsByProductIdResponse patchProductsByProductId(
        @PathParam("productId")
        String productId,
        @HeaderParam("Authorization")
        String authorization, Product entity)
        throws Exception
    ;

    /**
     * 
     * @param authorization
     *     The auth token for this request
     * @param productId
     *     
     */
    @DELETE
    @Path("{productId}")
    ProductsResource.DeleteProductsByProductIdResponse deleteProductsByProductId(
        @PathParam("productId")
        String productId,
        @HeaderParam("Authorization")
        String authorization)
        throws Exception
    ;

    /**
     * 
     * @param authorization
     *     The auth token for this request
     * @param productId
     *     
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     */
    @GET
    @Path("{productId}/agencies")
    @Produces({
        "application/json"
    })
    ProductsResource.GetProductsByProductIdAgenciesResponse getProductsByProductIdAgencies(
        @PathParam("productId")
        String productId,
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
     * @param productId
     *     
     * @param offset
     *     Skip over a number of elements by specifying an offset value for the query e.g. 20
     * @param limit
     *     Limit the number of elements on the response e.g. 80
     * @param entity
     *     
     */
    @POST
    @Path("{productId}/agencies")
    @Consumes("application/json")
    @Produces({
        "application/json"
    })
    ProductsResource.PostProductsByProductIdAgenciesResponse postProductsByProductIdAgencies(
        @PathParam("productId")
        String productId,
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

    public class DeleteProductsByProductIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private DeleteProductsByProductIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         */
        public static ProductsResource.DeleteProductsByProductIdResponse withOK() {
            Response.ResponseBuilder responseBuilder = Response.status(200);
            return new ProductsResource.DeleteProductsByProductIdResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.DeleteProductsByProductIdResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.DeleteProductsByProductIdResponse(responseBuilder.build());
        }

    }

    public class GetProductsByProductIdAgenciesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetProductsByProductIdAgenciesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.GetProductsByProductIdAgenciesResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.GetProductsByProductIdAgenciesResponse(responseBuilder.build());
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static ProductsResource.GetProductsByProductIdAgenciesResponse withJsonOK(Agencies entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.GetProductsByProductIdAgenciesResponse(responseBuilder.build());
        }

    }

    public class GetProductsByProductIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetProductsByProductIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static ProductsResource.GetProductsByProductIdResponse withJsonOK(Product entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.GetProductsByProductIdResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.GetProductsByProductIdResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.GetProductsByProductIdResponse(responseBuilder.build());
        }

    }

    public class GetProductsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private GetProductsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.GetProductsResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.GetProductsResponse(responseBuilder.build());
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static ProductsResource.GetProductsResponse withJsonOK(Products entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.GetProductsResponse(responseBuilder.build());
        }

    }

    public class PatchProductsByProductIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private PatchProductsByProductIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static ProductsResource.PatchProductsByProductIdResponse withJsonOK(Product entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.PatchProductsByProductIdResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.PatchProductsByProductIdResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.PatchProductsByProductIdResponse(responseBuilder.build());
        }

    }

    public class PostProductsByProductIdAgenciesResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private PostProductsByProductIdAgenciesResponse(Response delegate) {
            super(delegate);
        }

        /**
         * Created
         * 
         * @param entity
         *     
         */
        public static ProductsResource.PostProductsByProductIdAgenciesResponse withJsonCreated(Agency entity) {
            Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.PostProductsByProductIdAgenciesResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.PostProductsByProductIdAgenciesResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.PostProductsByProductIdAgenciesResponse(responseBuilder.build());
        }

    }

    public class PostProductsResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private PostProductsResponse(Response delegate) {
            super(delegate);
        }

        /**
         * Created
         * 
         * @param entity
         *     
         */
        public static ProductsResource.PostProductsResponse withJsonCreated(Product entity) {
            Response.ResponseBuilder responseBuilder = Response.status(201).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.PostProductsResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.PostProductsResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.PostProductsResponse(responseBuilder.build());
        }

    }

    public class PutProductsByProductIdResponse
        extends net.syscon.elite.web.api.resource.support.ResponseWrapper
    {


        private PutProductsByProductIdResponse(Response delegate) {
            super(delegate);
        }

        /**
         * OK
         * 
         * @param entity
         *     
         */
        public static ProductsResource.PutProductsByProductIdResponse withJsonOK(Product entity) {
            Response.ResponseBuilder responseBuilder = Response.status(200).header("Content-Type", "application/json");
            responseBuilder.entity(entity);
            return new ProductsResource.PutProductsByProductIdResponse(responseBuilder.build());
        }

        /**
         * Unauthorized
         * 
         */
        public static ProductsResource.PutProductsByProductIdResponse withUnauthorized() {
            Response.ResponseBuilder responseBuilder = Response.status(401);
            return new ProductsResource.PutProductsByProductIdResponse(responseBuilder.build());
        }

    }

}
