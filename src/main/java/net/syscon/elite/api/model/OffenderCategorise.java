package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.syscon.elite.api.support.CategorisationStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@ApiModel(description = "Summary of an offender counted as Establishment Roll - Reception")
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

    private String firstName;

    private String lastName;

    private LocalDate assessmentDate;

    private Integer assessmentSeq;

    private Long assessmentTypeId;

    private String assessStatus;

    @ApiModelProperty(required = true, value = "Where in the categorisation workflow the prisoner is")
    private CategorisationStatus status;

    static public OffenderCategorise deriveStatus(OffenderCategorise cat) {
        cat.status = (cat.getAssessStatus()) != null && cat.getAssessStatus().equals("P") ?
                CategorisationStatus.AWAITING_APPROVAL : CategorisationStatus.UNCATEGORISED;
        return cat;
    }
}
