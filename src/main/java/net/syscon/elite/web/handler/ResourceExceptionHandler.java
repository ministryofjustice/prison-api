package net.syscon.elite.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.service.ConfigException;
import net.syscon.elite.service.EntityAlreadyExistsException;
import net.syscon.elite.service.NoContentException;
import net.syscon.elite.service.RestServiceException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import javax.persistence.EntityNotFoundException;
import javax.transaction.NotSupportedException;
import javax.validation.ConstraintViolationException;


@Slf4j
public class ResourceExceptionHandler {

    public static ErrorResponse processResponse(final Exception ex) {
        final HttpStatus status;
        final String userMessage;
        var developerMessage = "";

        if (ex instanceof BadCredentialsException) {
            status = HttpStatus.UNAUTHORIZED;
            userMessage = "Invalid user credentials.";
            developerMessage = "Authentication credentials provided are not valid.";
            log.warn("Invalid credentials.", ex);
        } else if (ex instanceof EntityNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof EmptyResultDataAccessException) {
            status = HttpStatus.NOT_FOUND;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof EntityAlreadyExistsException) {
            status = HttpStatus.CONFLICT;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            userMessage = ex.getMessage();
            log.warn("Insufficient privileges to access resource.", ex);
        } else if (ex instanceof InvalidDataAccessApiUsageException) {
            status = HttpStatus.BAD_REQUEST;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof DuplicateKeyException) {
            status = HttpStatus.BAD_REQUEST;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof ConstraintViolationException) {
            status = HttpStatus.BAD_REQUEST;
            developerMessage = ex.toString();
            userMessage = ex.getMessage();
            log.warn("JSR303 error.", ex);
        } else if (ex instanceof ConfigException) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            userMessage = ex.getMessage();
            log.error("Internal Server Error", ex);
        } else if (ex instanceof NotSupportedException) {
            userMessage = "Media Type not supported - should be application/json";
            status = HttpStatus.NOT_IMPLEMENTED;
        } else if (ex instanceof RestServiceException) {
            status = ((RestServiceException) ex).getResponseStatus();
            userMessage = ex.getMessage();
            log.error("Rest service error", ex);
        } else if (ex instanceof NoContentException) {
            status = HttpStatus.NO_CONTENT;
            userMessage = "No content returned";
            developerMessage = ex.getMessage();
            log.info(developerMessage);
        } else if (ex instanceof IllegalStateException) {
            status = HttpStatus.CONFLICT;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            userMessage = ex.getMessage();
            log.info(userMessage);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            userMessage = "An internal error has occurred - please try again later.";
            developerMessage = ex.getMessage();
            log.error("Internal Server Error", ex);
        }

        return ErrorResponse.builder()
                .status(status.value())
                .userMessage(userMessage)
                .developerMessage(developerMessage)
                .build();
    }
}
