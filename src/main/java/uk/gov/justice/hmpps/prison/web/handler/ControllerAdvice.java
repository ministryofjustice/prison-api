package uk.gov.justice.hmpps.prison.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.GenericJDBCException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.justice.hmpps.prison.api.model.ErrorResponse;
import uk.gov.justice.hmpps.prison.service.BadRequestException;
import uk.gov.justice.hmpps.prison.service.ConflictingRequestException;
import uk.gov.justice.hmpps.prison.service.EntityAlreadyExistsException;
import uk.gov.justice.hmpps.prison.service.EntityNotFoundException;
import uk.gov.justice.hmpps.prison.service.NoContentException;

import javax.persistence.EntityExistsException;
import javax.validation.ValidationException;
import java.sql.SQLException;
import java.util.stream.Collectors;

import static java.lang.String.format;


@RestControllerAdvice(
    basePackages = {"uk.gov.justice.hmpps.prison.api.resource", "uk.gov.justice.hmpps.nomis.api.resource.controller"}
)
@Slf4j
public class ControllerAdvice {

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<byte[]> handleRestClientResponseException(final RestClientResponseException e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
            .status(e.getRawStatusCode())
            .body(e.getResponseBodyAsByteArray());
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(final RestClientException e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse
                .builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .developerMessage(e.getMostSpecificCause().getMessage())
                .build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(final AccessDeniedException e) {
        log.debug("Forbidden (403) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.FORBIDDEN.value())
                .build());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(final ValidationException e) {
        log.debug("Bad Request (400) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(final MissingServletRequestParameterException e) {
        log.debug("Bad Request (400) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(final EntityNotFoundException e) {
        log.debug("Not found (404) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEntityAlreadyExistsException(final EntityAlreadyExistsException e) {
        log.debug("Already exists (409) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(ConflictingRequestException.class)
    public ResponseEntity<ErrorResponse> handleConflictingRequestException(final ConflictingRequestException e) {
        log.debug("Conflict (409) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .errorCode(e.getErrorCode())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorResponse> handleEmptyResultDataAccessException(final EmptyResultDataAccessException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(javax.persistence.EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(final javax.persistence.EntityNotFoundException e) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .developerMessage(e.getCause().getMessage())
                .build());
    }


    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ErrorResponse> handleEntityExistsException(final EntityExistsException e) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientErrorException(final HttpClientErrorException e) {
        return ResponseEntity
            .status(e.getStatusCode())
            .body(ErrorResponse
                .builder()
                .userMessage(e.getStatusText())
                .status(e.getStatusCode().value())
                .developerMessage(e.getMostSpecificCause().getMessage())
                .build());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(final TypeMismatchException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMostSpecificCause().getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(e.getMostSpecificCause().getMessage())
                .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(final IllegalStateException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDataAccessApiUsageException(final InvalidDataAccessApiUsageException e) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMostSpecificCause().getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(e.getMostSpecificCause().getMessage())
                .build());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageConversionException(final HttpMessageConversionException e) {
        log.error("A malformed request was rejected due to exception", e.getMostSpecificCause());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage("Malformed request")
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage("Malformed request")
                .build());
    }


    @ExceptionHandler(NoContentException.class)
    public ResponseEntity<ErrorResponse> handleNoContentException(final NoContentException e) {
        log.debug("No content (204) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .body(ErrorResponse
                .builder()
                .userMessage("No content returned")
                .status(HttpStatus.NO_CONTENT.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        final var errors = e.getBindingResult().getFieldErrors()
            .stream()
            .map(error -> "Field: " + error.getField() + " - " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(errors)
                .status(HttpStatus.BAD_REQUEST.value())
                .developerMessage(errors)
                .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(final BadRequestException e) {
        log.debug("Bad Request (400) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.BAD_REQUEST.value())
                 .errorCode(e.getErrorCode())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ErrorResponse> handleServiceException(final DuplicateKeyException e) {
        log.debug("Conflict (409) returned with message {}", e.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse
                .builder()
                .userMessage(e.getMessage())
                .status(HttpStatus.CONFLICT.value())
                .developerMessage(e.getMessage())
                .build());
    }

    @ExceptionHandler(JpaSystemException.class)
    public ResponseEntity<ErrorResponse> handleJpaException(final JpaSystemException e) {
        try {
            if (e.getCause() instanceof GenericJDBCException && e.getCause().getCause() instanceof SQLException) {
                return handleJpaSqlException(e);
            }
        } catch (Exception ex) {
            // we don't know the shape of this exception - handling below
        }

        // If this ever happens we should investigate and see if we can handle it in a nicer way
        log.info("Error received from JPA caused by {}", e.getRootCause(), e);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse
                .builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .userMessage("An unexpected error occurred")
                .developerMessage(format("Root cause: %s", e.getRootCause()))
                .build());
    }

    private ResponseEntity<ErrorResponse> handleJpaSqlException(final JpaSystemException e) {
        log.info("Error received from JPA caused by {}", e.getCause().getCause().getMessage(), e);
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse
                .builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .userMessage(getJpaSqlExceptionUserMessage(e))
                .developerMessage(getJpaSqlExceptionDeveloperMessage(e))
                .build());
    }

    private String getJpaSqlExceptionUserMessage(final JpaSystemException e) {
        return e.getCause().getCause()
            .getMessage()
            .split("\n")[0]
            .split("^ORA-\\d{5,6}: ")[1];
    }

    private String getJpaSqlExceptionDeveloperMessage(final JpaSystemException e) {
        return e.getCause().getCause().getMessage();
    }

}
