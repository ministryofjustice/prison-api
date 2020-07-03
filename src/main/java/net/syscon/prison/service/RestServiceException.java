package net.syscon.prison.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class RestServiceException extends RuntimeException {
    private final HttpStatus responseStatus;
    private final String detailedMessage;

    // Reverse lookup
    private static final Map<String, Exception2Status> lookup = new HashMap<>();

    static {
        for (final var mapping : Exception2Status.values()) {
            lookup.put(mapping.exceptionName, mapping);
        }
    }

    public static RestServiceException forDataAccessException(final DataAccessException ex) {
        final var rootCauseMessage = (ex.getRootCause() != null) ? ex.getRootCause().getMessage() : ex.getMessage();

        final var simpleMessage = StringUtils.removePattern(ex.getMessage(), "; nested exception is.+$");

        return new RestServiceException(Exception2Status.get(ex).getResponseStatus(), simpleMessage, rootCauseMessage);
    }

    public RestServiceException(final HttpStatus responseStatus, final String message, final String detailedMessage) {
        super(message);

        Validate.notNull(responseStatus);

        this.responseStatus = responseStatus;
        this.detailedMessage = detailedMessage;
    }

    public HttpStatus getResponseStatus() {
        return responseStatus;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    enum Exception2Status {
        EMPTY_RESULT("org.springframework.dao.EmptyResultDataAccessException", HttpStatus.NOT_FOUND),
        INVALID_USAGE("org.springframework.dao.InvalidDataAccessApiUsageException", HttpStatus.BAD_REQUEST),
        DATA_INTEGRITY_VIOLATION("org.springframework.dao.DataIntegrityViolationException", HttpStatus.BAD_REQUEST),
        DATA_RETRIEVAL_FAILURE("org.springframework.dao.DataRetrievalFailureException", HttpStatus.NOT_FOUND),
        TOO_MANY_RESULTS("org.springframework.dao.IncorrectResultSizeDataAccessException", HttpStatus.BAD_REQUEST);

        private final String exceptionName;
        private final HttpStatus responseStatus;

        Exception2Status(final String exceptionName, final HttpStatus responseStatus) {
            this.exceptionName = exceptionName;
            this.responseStatus = responseStatus;
        }

        public String getExceptionName() {
            return exceptionName;
        }

        public HttpStatus getResponseStatus() {
            return responseStatus;
        }

        public static Exception2Status get(final Exception ex) {
            return lookup.get(ex.getClass().getName());
        }
    }
}
