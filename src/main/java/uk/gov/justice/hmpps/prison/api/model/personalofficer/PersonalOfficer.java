package uk.gov.justice.hmpps.prison.api.model.personalofficer;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PersonalOfficer(
    String agencyId,
    String offenderNo,
    Long staffId,
    String userId,
    LocalDateTime assigned,
    LocalDateTime created,
    String createdBy
) {
}
