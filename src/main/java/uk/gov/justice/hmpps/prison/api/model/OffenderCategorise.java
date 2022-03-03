package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.hmpps.prison.api.support.CategorisationStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Prisoner with categorisation data")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OffenderCategorise {

    @ApiModelProperty(required = true, value = "Display Prisoner Number")
    private String offenderNo;

    @NotNull
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Prisoner First Name")
    private String firstName;

    @ApiModelProperty(required = true, value = "Prisoner Last Name")
    private String lastName;

    @ApiModelProperty(value = "Categorisation date if any")
    private LocalDate assessmentDate;

    @ApiModelProperty(value = "Date categorisation was approved if any")
    private LocalDate approvalDate;

    @ApiModelProperty(value = "Sequence number within booking")
    private Integer assessmentSeq;

    @ApiModelProperty(value = "assessment type", allowableValues = "CATEGORY")
    private Long assessmentTypeId;

    @ApiModelProperty(value = "Categorisation status", allowableValues = "P,A,I,null")
    private String assessStatus;

    @ApiModelProperty(value = "Categoriser First Name")
    private String categoriserFirstName;

    @ApiModelProperty(value = "Categoriser Last Name")
    private String categoriserLastName;

    @ApiModelProperty(value = "Approver First Name if any")
    private String approverFirstName;

    @ApiModelProperty(value = "Approver Last Name if any")
    private String approverLastName;

    @ApiModelProperty(value = "Categorisation")
    private String category;

    @ApiModelProperty(value = "Next Review Date - for recategorisations")
    private LocalDate nextReviewDate;

    @ApiModelProperty(required = true, value = "Where in the categorisation workflow the prisoner is")
    private CategorisationStatus status;

    static public OffenderCategorise deriveStatus(final OffenderCategorise cat) {
        cat.status = (cat.getAssessStatus()) != null && cat.getAssessStatus().equals("P") ?
                CategorisationStatus.AWAITING_APPROVAL : CategorisationStatus.UNCATEGORISED;
        return cat;
    }
}
