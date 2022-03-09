package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@ApiModel(description = "Reasonable Adjustment")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ReasonableAdjustment {

    @ApiModelProperty(value = "Treatment Code", position = 1, example = "WHEELCHR_ACC")
    private String treatmentCode;

    @ApiModelProperty(value = "Comment Text", position = 2, example = "abcd")
    private String commentText;

    @ApiModelProperty(value = "Start Date", position = 3, example = "2010-06-21")
    private LocalDate startDate;

    @ApiModelProperty(value = "End Date", position = 4, example = "2010-06-21")
    private LocalDate endDate;

    @ApiModelProperty(value = "The agency id where the adjustment was created", position = 5, example = "LEI")
    private String agencyId;

    @ApiModelProperty(value = "Treatment Description", position = 6, example = "Wheelchair accessibility")
    private String treatmentDescription;
}
