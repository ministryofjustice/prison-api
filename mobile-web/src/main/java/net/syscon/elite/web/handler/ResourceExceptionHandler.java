package net.syscon.elite.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.support.OperationResponse;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import javax.ws.rs.NotFoundException;
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
    public Response toResponse(Exception ex) {
        int status;
        String userMessage;
        String developerMessage = "";

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
        } else if (ex instanceof AccessDeniedException) {
            status = Response.Status.FORBIDDEN.getStatusCode();
            userMessage = "You do not have sufficient privileges to access this resource.";
            log.warn("Insufficient privileges to access resource.", ex);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            userMessage = "An internal error has occurred - please try again later.";
            log.error("Internal Server Error", ex);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .userMessage(userMessage)
                .developerMessage(developerMessage)
                .build();

        return OperationResponse.respondErrorWithApplicationJson(errorResponse);
    }
}
