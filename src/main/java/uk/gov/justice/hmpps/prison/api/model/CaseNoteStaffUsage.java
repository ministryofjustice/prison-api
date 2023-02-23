package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Case Note Type Staff Usage
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note Type Staff Usage")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@EqualsAndHashCode
@Data
public class CaseNoteStaffUsage {
    @Schema(required = true, description = "Staff ID", example = "2341232")
    @NotNull
    private Integer staffId;

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

    public CaseNoteStaffUsage(@NotNull Integer staffId, @NotBlank String caseNoteType, @NotBlank String caseNoteSubType, @NotNull Integer numCaseNotes, @NotNull LocalDateTime latestCaseNote) {
        this.staffId = staffId;
        this.caseNoteType = caseNoteType;
        this.caseNoteSubType = caseNoteSubType;
        this.numCaseNotes = numCaseNotes;
        this.latestCaseNote = latestCaseNote;
    }

    public CaseNoteStaffUsage() {
    }
}
