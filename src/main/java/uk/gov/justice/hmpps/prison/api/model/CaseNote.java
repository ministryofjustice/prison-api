package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * Case Note
 **/
@SuppressWarnings("unused")
@Schema(description = "Case Note")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
@ToString(of = {"caseNoteId", "bookingId", "type", "subType", "source", "authorName", "agencyId", "occurrenceDateTime"})
public class CaseNote {

    @Schema(requiredMode = REQUIRED, description = "Case Note Id (unique)", example = "12311312")
    @NotNull
    private Long caseNoteId;

    @Schema(requiredMode = REQUIRED, description = "Booking Id of offender", example = "512321")
    @NotNull
    private Long bookingId;

    @Schema(requiredMode = REQUIRED, description = "Case Note Type", example = "KA")
    @NotBlank
    private String type;

    @Schema(description = "Case Note Type Description", example = "Key Worker Activity")
    private String typeDescription;

    @Schema(requiredMode = REQUIRED, description = "Case Note Sub Type", example = "KS")
    @NotBlank
    private String subType;

    @Schema(description = "Case Note Sub Type Description", example = "Key Worker Session")
    private String subTypeDescription;

    @Schema(requiredMode = REQUIRED, description = "Source Type", example = "INST")
    @NotBlank
    private String source;

    @Schema(requiredMode = REQUIRED, description = "Date and Time of Case Note creation", example = "2017-10-31T01:30:00")
    @NotNull
    private LocalDateTime creationDateTime;

    @Schema(requiredMode = REQUIRED, description = "Date and Time of when case note contact with offender was made", example = "2017-10-31T01:30:00")
    @NotNull
    private LocalDateTime occurrenceDateTime;

    @Schema(requiredMode = REQUIRED, description = "Id of staff member who created case note", example = "321241")
    @NotNull
    private Long staffId;

    @Schema(requiredMode = REQUIRED, description = "Name of staff member who created case note (lastname, firstname)", example = "Smith, John")
    @NotBlank
    private String authorName;

    @Schema(requiredMode = REQUIRED, description = "Case Note Text", example = "This is some text")
    @NotBlank
    private String text;

    @Schema(requiredMode = REQUIRED, description = "The initial case note information that was entered", example = "This is some text")
    @NotBlank
    private String originalNoteText;

    @Schema(description = "Agency Code where Case Note was made.", example = "MDI")
    private String agencyId;

    @Schema(requiredMode = REQUIRED, description = "Ordered list of amendments to the case note (oldest first)")
    @NotNull
    @Builder.Default
    private List<CaseNoteAmendment> amendments = new ArrayList<>();

}
