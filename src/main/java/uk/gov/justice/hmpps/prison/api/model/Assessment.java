package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

/**
 * Assessment
 **/
@SuppressWarnings("unused")
@Schema(description = "Assessment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Assessment {
    @JsonIgnore
    private Map<String, Object> additionalProperties;

    @NotNull
    @Schema(description = "Booking number", example = "123456")
    private Long bookingId;

    @NotBlank
    @Schema(description = "Offender number (e.g. NOMS Number).", example = "GV09876N")
    private String offenderNo;

    @NotBlank
    @Schema(description = "Classification code", example = "C")
    private String classificationCode;

    @NotBlank
    @Schema(description = "Classification description", example = "Cat C")
    private String classification;

    @NotBlank
    @Schema(description = "Identifies the type of assessment", example = "CATEGORY")
    private String assessmentCode;

    @NotBlank
    @Schema(description = "Assessment description", example = "Categorisation")
    private String assessmentDescription;

    @NotNull
    @Schema(description = "Indicates the presence of a cell sharing alert")
    private boolean cellSharingAlertFlag;

    @NotNull
    @Schema(description = "Date assessment was created", example = "2018-02-11")
    private LocalDate assessmentDate;

    @NotNull
    @Schema(description = "Date of next review", example = "2018-02-11")
    private LocalDate nextReviewDate;

    @Schema(description = "Date of assessment approval", example = "2018-02-11")
    private LocalDate approvalDate;

    @Schema(description = "The assessment creation agency id", example = "MDI")
    private String assessmentAgencyId;

    @Schema(description = "The status of the assessment", example = "A", allowableValues = {"P","A","I"})
    private String assessmentStatus;

    @Schema(description = "Sequence number of assessment within booking", example = "1")
    private Integer assessmentSeq;

    @Schema(description = "Comment from assessor", example = "Comment details")
    private String assessmentComment;

    @Schema(description = "Staff member who made the assessment", example = "130000")
    private Long assessorId;

    @Schema(description = "Username who made the assessment", example = "NGK33Y")
    private String assessorUser;
}
