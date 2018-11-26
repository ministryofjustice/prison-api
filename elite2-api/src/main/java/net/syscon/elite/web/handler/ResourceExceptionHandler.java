package net.syscon.elite.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.support.OperationResponse;
import net.syscon.elite.service.AllocationException;
import net.syscon.elite.service.ConfigException;
import net.syscon.elite.service.EntityAlreadyExistsException;
import net.syscon.elite.service.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.NotSupportedException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

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
        } else if (ex instanceof EntityAlreadyExistsException) {
            status = Response.Status.CONFLICT.getStatusCode();
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof AccessDeniedException) {
            status = Response.Status.FORBIDDEN.getStatusCode();
            userMessage = ex.getMessage();
            log.warn("Insufficient privileges to access resource.", ex);
        } else if (ex instanceof ConstraintViolationException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            userMessage = formatConstraintErrors(ex);
            log.warn("JSR303 error.", ex);
        } else if (ex instanceof BadRequestException) {
            status = Response.Status.BAD_REQUEST.getStatusCode();
            userMessage = ex.getMessage();
            log.warn("Client submitted invalid request.", ex);
        } else if (ex instanceof ConfigException) {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            userMessage = ex.getMessage();
            log.error("Internal Server Error", ex);
        } else if (ex instanceof AllocationException) {
            status = Response.Status.CONFLICT.getStatusCode();
            userMessage = ex.getMessage();
            log.error("Resource Conflict Error", ex);
        } else if (ex instanceof NotSupportedException) {
            status = Response.Status.NOT_IMPLEMENTED.getStatusCode();
            userMessage = ex.getMessage();
            log.error("Service Not Implemented", ex);
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

    private static String formatConstraintErrors(Exception ex) {
        StringBuilder sb = new StringBuilder();
        final Set<ConstraintViolation<?>> constraintViolations = ((ConstraintViolationException) ex).getConstraintViolations();
        if (CollectionUtils.isEmpty(constraintViolations)) {
            return "";
        }
        for (ConstraintViolation<?> cv : constraintViolations) {
            sb.append(cv.getMessage());
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
