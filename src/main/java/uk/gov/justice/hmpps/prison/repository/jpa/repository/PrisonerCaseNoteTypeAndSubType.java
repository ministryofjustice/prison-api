package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import java.time.LocalDateTime;

public record PrisonerCaseNoteTypeAndSubType(Long bookingId, String type, String subType,
                                             LocalDateTime occurrenceDateTime) {
}
