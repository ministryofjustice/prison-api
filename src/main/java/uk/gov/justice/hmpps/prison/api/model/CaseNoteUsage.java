package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Case Note Type Usage
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note Type Usage")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@Data
public class CaseNoteUsage {
    @Schema(required = true, description = "Offender No", example = "ZWE12A")
    @NotBlank
    private String offenderNo;

    @Schema(required = true, description = "Case Note Type", example = "KA")
    @NotBlank
    private String caseNoteType;

    @Schema(required = true, description = "Case Note Sub Type", example = "KS")
    @NotBlank
    private String caseNoteSubType;

    @Schema(required = true, description = "Number of case notes of this type/subtype", example = "5")
    @NotNull
    private Integer numCaseNotes;

    @Schema(required = true, description = "Last case note of this type", example = "2018-12-01T14:55:23")
    @NotNull
    private LocalDateTime latestCaseNote;

    public CaseNoteUsage(@NotBlank String offenderNo, @NotBlank String caseNoteType, @NotBlank String caseNoteSubType, @NotNull Integer numCaseNotes, @NotNull LocalDateTime latestCaseNote) {
        this.offenderNo = offenderNo;
        this.caseNoteType = caseNoteType;
        this.caseNoteSubType = caseNoteSubType;
        this.numCaseNotes = numCaseNotes;
        this.latestCaseNote = latestCaseNote;
    }

    public CaseNoteUsage() {
    }
}
