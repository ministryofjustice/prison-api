package uk.gov.justice.hmpps.prison.service;

public class BadRequestException extends RuntimeException {
    public BadRequestException(final String message) {
        super(message);
    }
}
