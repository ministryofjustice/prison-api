package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.support.CategorisationStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "Prisoner with categorisation data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderCategorise {

    @Schema(required = true, description = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    @Schema(required = true, description = "Prisoner First Name")
    private String firstName;

    @Schema(required = true, description = "Prisoner Last Name")
    private String lastName;

    @Schema(description = "Categorisation date if any")
    private LocalDate assessmentDate;

    @Schema(description = "Date categorisation was approved if any")
    private LocalDate approvalDate;

    @Schema(description = "Sequence number within booking")
    private Integer assessmentSeq;

    @Schema(description = "assessment type", allowableValues = "CATEGORY")
    private Long assessmentTypeId;

    @Schema(description = "Categorisation status", allowableValues = "P,A,I,null")
    private String assessStatus;

    @Schema(description = "Categoriser First Name")
    private String categoriserFirstName;

    @Schema(description = "Categoriser Last Name")
    private String categoriserLastName;

    @Schema(description = "Approver First Name if any")
    private String approverFirstName;

    @Schema(description = "Approver Last Name if any")
    private String approverLastName;

    @Schema(description = "Categorisation")
    private String category;

    @Schema(description = "Next Review Date - for recategorisations")
    private LocalDate nextReviewDate;

    @Schema(required = true, description = "Where in the categorisation workflow the prisoner is")
    private CategorisationStatus status;

    static public OffenderCategorise deriveStatus(final OffenderCategorise cat) {
        cat.status = (cat.getAssessStatus()) != null && cat.getAssessStatus().equals("P") ?
                CategorisationStatus.AWAITING_APPROVAL : CategorisationStatus.UNCATEGORISED;
        return cat;
    }
}
