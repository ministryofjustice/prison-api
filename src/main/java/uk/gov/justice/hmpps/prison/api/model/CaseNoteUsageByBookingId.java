package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Case Note Type Usage By Booking Id")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@Data
public class CaseNoteUsageByBookingId {
    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Booking Id", example = "123456")
    private Long bookingId;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Case Note Type", example = "KA")
    private String caseNoteType;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Case Note Sub Type", example = "KS")
    private String caseNoteSubType;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Number of case notes of this type/subtype", example = "5")
    private Long numCaseNotes;

    @Schema(requiredMode = RequiredMode.REQUIRED, description = "Last case note of this type", example = "2018-12-01T14:55:23")
    private LocalDateTime latestCaseNote;

    public CaseNoteUsageByBookingId(Long bookingId, String caseNoteType, String caseNoteSubType, Long numCaseNotes, LocalDateTime latestCaseNote) {
        this.bookingId = bookingId;
        this.caseNoteType = caseNoteType;
        this.caseNoteSubType = caseNoteSubType;
        this.numCaseNotes = numCaseNotes;
        this.latestCaseNote = latestCaseNote;
    }
}
