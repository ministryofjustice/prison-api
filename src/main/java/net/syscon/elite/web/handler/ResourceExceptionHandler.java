package net.syscon.elite.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.support.OperationResponse;
import net.syscon.elite.service.*;
import org.glassfish.jersey.server.ParamException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Slf4j
public class ResourceExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    public Response toResponse(final Exception ex) {
        return OperationResponse.respondErrorWithApplicationJson(processResponse(ex));
    }

    public static ErrorResponse processResponse(final Exception ex) {
        final int status;
        final String userMessage;
        var developerMessage = "";

        if (ex instanceof BadCredentialsException) {
            status = Response.Status.UNAUTHORIZED.getStatusCode();
            userMessage = "Invalid user credentials.";
            developerMessage = "Authentication credentials provided are not valid.";
            log.warn("Invalid credentials.", ex);
        } else if (ex instanceof NotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            userMessage = "Resource not found.";
            developerMessage = "Resource not found.";
            log.warn("Resource Not Found - an incorrect resource path/uri is being used.", ex);
        } else if (ex instanceof EntityNotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof EmptyResultDataAccessException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof EntityAlreadyExistsException) {
            status = Response.Status.CONFLICT.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof AccessDeniedException) {
            status = Response.Status.FORBIDDEN.getStatusCode();
            userMessage = ex.getMessage();
            log.warn("Insufficient privileges to access resource.", ex);
        } else if (ex instanceof InvalidDataAccessApiUsageException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof DuplicateKeyException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof ConstraintViolationException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            developerMessage = ex.toString();
            userMessage = ex.getMessage();
            log.warn("JSR303 error.", ex);
        } else if (ex instanceof BadRequestException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            userMessage = ex.getMessage();
            log.warn("Client submitted invalid request.", ex);
        } else if (ex instanceof ConfigException) {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            userMessage = ex.getMessage();
            log.error("Internal Server Error", ex);
        } else if (ex instanceof NotSupportedException) {
            userMessage = "Media Type not supported - should be application/json";
            status = Response.Status.NOT_IMPLEMENTED.getStatusCode();
        } else if (ex instanceof RestServiceException) {
            status = ((RestServiceException) ex).getResponseStatus().getStatusCode();
            userMessage = ex.getMessage();
            log.error("Rest service error", ex);
        } else if (ex instanceof NoContentException) {
            status = Response.Status.NO_CONTENT.getStatusCode();
            userMessage = "No content returned";
            developerMessage = ex.getMessage();
            log.info(developerMessage);
        } else if (ex instanceof IllegalStateException) {
            status = Response.Status.CONFLICT.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof IllegalArgumentException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof ParamException.QueryParamException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            developerMessage = ex.getMessage();
            userMessage = "Parameter exception (invalid date, time, format, type)";
            log.info(developerMessage);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            userMessage = "An internal error has occurred - please try again later.";
            developerMessage = ex.getMessage();
            log.error("Internal Server Error", ex);
        }

        return ErrorResponse.builder()
                .status(status)
                .userMessage(userMessage)
                .developerMessage(developerMessage)
                .build();
    }
}
