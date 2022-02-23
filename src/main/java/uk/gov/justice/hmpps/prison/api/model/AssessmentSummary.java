package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@SuppressWarnings("unused")
@Schema(description = "AssessmentSummary")
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@EqualsAndHashCode
public class AssessmentSummary {

    @NotNull
    @Schema(description = "Booking number", example = "123456")
    private Long bookingId;

    @NotNull
    @Schema(description = "Sequence number of assessment within booking", example = "1")
    private Integer assessmentSeq;

    @NotBlank
    @Schema(description = "Offender number (e.g. NOMS Number).", example = "GV09876N")
    private String offenderNo;

    @Schema(description = "Classification code. This will not have a value if the assessment is incomplete or pending", example = "STANDARD")
    private String classificationCode;

    @NotBlank
    @Schema(description = "Identifies the type of assessment", example = "CSR")
    private String assessmentCode;

    @NotNull
    @Schema(description = "Indicates whether this is a CSRA assessment")
    private boolean cellSharingAlertFlag;

    @NotNull
    @Schema(description = "Date assessment was created", example = "2018-02-11")
    private LocalDate assessmentDate;

    @Schema(description = "The assessment creation agency id", example = "MDI")
    private String assessmentAgencyId;

    @Schema(description = "Comment from assessor", example = "Comment details")
    private String assessmentComment;

    @Schema(description = "Username who made the assessment", example = "NGK33Y")
    private String assessorUser;

    @Schema(description = "Date of next review", example = "2018-02-11")
    private LocalDate nextReviewDate;
}
