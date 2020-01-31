package net.syscon.elite.api.resource;

import io.swagger.annotations.*;
import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.support.ResponseDelegate;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Api(tags = {"/access-roles"})
@SuppressWarnings("unused")
public interface AccessRoleResource {

    @GET
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "List of access roles", notes = "List of access roles", nickname = "getAccessRoles")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = AccessRole.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Invalid request.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 404, message = "Requested resource not found.", response = ErrorResponse.class, responseContainer = "List"),
            @ApiResponse(code = 500, message = "Unrecoverable error occurred whilst processing request.", response = ErrorResponse.class, responseContainer = "List")})
    GetAccessRolesResponse getAccessRoles(@ApiParam(value = "Include admin roles", required = true, defaultValue = "false") @QueryParam("includeAdmin") boolean includeAdmin);

    @POST
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Create new access role.", notes = "Create new access role.", nickname = "createAccessRole")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. role code not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to create an access role.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Parent access role not found.", response = ErrorResponse.class),
            @ApiResponse(code = 409, message = "A role already exists with the provided role code.", response = ErrorResponse.class)})
    CreateAccessRoleResponse createAccessRole(@ApiParam(value = "", required = true) AccessRole body);

    @PUT
    @Path("/")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @ApiOperation(value = "Update the access role.", notes = "Update the access role.", nickname = "updateAccessRole")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = ""),
            @ApiResponse(code = 400, message = "Invalid request - e.g. role code not provided.", response = ErrorResponse.class),
            @ApiResponse(code = 403, message = "Forbidden - user not authorised to update an access role.", response = ErrorResponse.class),
            @ApiResponse(code = 404, message = "Access role not found.", response = ErrorResponse.class)})
    UpdateAccessRoleResponse updateAccessRole(@ApiParam(value = "", required = true) AccessRole body);

    class GetAccessRolesResponse extends ResponseDelegate {

        private GetAccessRolesResponse(final Response response) {
            super(response);
        }

        private GetAccessRolesResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static GetAccessRolesResponse respond200WithApplicationJson(final List<AccessRole> entity) {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAccessRolesResponse(responseBuilder.build(), entity);
        }

        public static GetAccessRolesResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAccessRolesResponse(responseBuilder.build(), entity);
        }

        public static GetAccessRolesResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAccessRolesResponse(responseBuilder.build(), entity);
        }

        public static GetAccessRolesResponse respond500WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(500)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new GetAccessRolesResponse(responseBuilder.build(), entity);
        }
    }

    class CreateAccessRoleResponse extends ResponseDelegate {

        private CreateAccessRoleResponse(final Response response) {
            super(response);
        }

        private CreateAccessRoleResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static CreateAccessRoleResponse respond201WithApplicationJson() {
            final var responseBuilder = Response.status(201)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            return new CreateAccessRoleResponse(responseBuilder.build());
        }

        public static CreateAccessRoleResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateAccessRoleResponse(responseBuilder.build(), entity);
        }

        public static CreateAccessRoleResponse respond403WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(403)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateAccessRoleResponse(responseBuilder.build(), entity);
        }

        public static CreateAccessRoleResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateAccessRoleResponse(responseBuilder.build(), entity);
        }

        public static CreateAccessRoleResponse respond409WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(409)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new CreateAccessRoleResponse(responseBuilder.build(), entity);
        }
    }

    class UpdateAccessRoleResponse extends ResponseDelegate {

        private UpdateAccessRoleResponse(final Response response) {
            super(response);
        }

        private UpdateAccessRoleResponse(final Response response, final Object entity) {
            super(response, entity);
        }

        public static UpdateAccessRoleResponse respond200WithApplicationJson() {
            final var responseBuilder = Response.status(200)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            return new UpdateAccessRoleResponse(responseBuilder.build());
        }

        public static UpdateAccessRoleResponse respond400WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(400)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new UpdateAccessRoleResponse(responseBuilder.build(), entity);
        }

        public static UpdateAccessRoleResponse respond403WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(403)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new UpdateAccessRoleResponse(responseBuilder.build(), entity);
        }

        public static UpdateAccessRoleResponse respond404WithApplicationJson(final ErrorResponse entity) {
            final var responseBuilder = Response.status(404)
                    .header("Content-Type", MediaType.APPLICATION_JSON);
            responseBuilder.entity(entity);
            return new UpdateAccessRoleResponse(responseBuilder.build(), entity);
        }
    }
}
