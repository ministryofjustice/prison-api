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

    @Schema(required = true, description = "Case Note Id (unique)", example = "12311312")
    @NotNull
    private Long caseNoteId;

    @Schema(required = true, description = "Booking Id of offender", example = "512321")
    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Case Note Type", example = "KA")
    @NotBlank
    private String type;

    @Schema(description = "Case Note Type Description", example = "Key Worker Activity")
    private String typeDescription;

    @Schema(required = true, description = "Case Note Sub Type", example = "KS")
    @NotBlank
    private String subType;

    @Schema(description = "Case Note Sub Type Description", example = "Key Worker Session")
    private String subTypeDescription;

    @Schema(required = true, description = "Source Type", example = "INST")
    @NotBlank
    private String source;

    @Schema(required = true, description = "Date and Time of Case Note creation", example = "2017-10-31T01:30:00")
    @NotNull
    private LocalDateTime creationDateTime;

    @Schema(required = true, description = "Date and Time of when case note contact with offender was made", example = "2017-10-31T01:30:00")
    @NotNull
    private LocalDateTime occurrenceDateTime;

    @Schema(required = true, description = "Id of staff member who created case note", example = "321241")
    @NotNull
    private Long staffId;

    @Schema(required = true, description = "Name of staff member who created case note (lastname, firstname)", example = "Smith, John")
    @NotBlank
    private String authorName;

    @Schema(required = true, description = "Case Note Text", example = "This is some text")
    @NotBlank
    private String text;

    @Schema(required = true, description = "The initial case note information that was entered", example = "This is some text")
    @NotBlank
    private String originalNoteText;

    @Schema(description = "Agency Code where Case Note was made.", example = "MDI")
    private String agencyId;

    @Schema(required = true, description = "Ordered list of amendments to the case note (oldest first)")
    @NotNull
    @Builder.Default
    private List<CaseNoteAmendment> amendments = new ArrayList<>();

}
