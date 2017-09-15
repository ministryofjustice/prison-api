package net.syscon.elite.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.v2.api.model.ErrorResponse;
import net.syscon.elite.v2.api.support.OperationResponse;
import org.springframework.security.access.AccessDeniedException;

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

        if (ex instanceof NotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            userMessage = "Resource not found.";
            developerMessage = "An incorrect resource path/uri is being used - please correct and try again.";
        } else if (ex instanceof EntityNotFoundException) {
            status = Response.Status.NOT_FOUND.getStatusCode();
            userMessage = "Resource with id [" + "] not found.";
        } else if (ex instanceof AccessDeniedException) {
            status = Response.Status.FORBIDDEN.getStatusCode();
            userMessage = "You do not have sufficient privileges to access this resource.";
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            userMessage = "An internal error has occurred - please try again later.";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status)
                .userMessage(userMessage)
                .developerMessage(developerMessage)
                .build();

        return OperationResponse.respondErrorWithApplicationJson(errorResponse);
    }
}
