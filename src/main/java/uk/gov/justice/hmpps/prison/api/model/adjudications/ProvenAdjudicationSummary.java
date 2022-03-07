package uk.gov.justice.hmpps.prison.api.model.adjudications;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@ApiModel(description = "Proven Adjudication Summary for offender")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProvenAdjudicationSummary {

    @ApiModelProperty(required = true, value = "Offender Booking Id")
    private Long bookingId;

    @ApiModelProperty(required = true, value = "Number of proven adjudications")
    private Integer provenAdjudicationCount;

}
