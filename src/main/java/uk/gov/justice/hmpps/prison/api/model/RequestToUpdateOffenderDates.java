package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Update Offender Dates Request")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RequestToUpdateOffenderDates {

    @ApiModelProperty(required = true, value = "DPS/NOMIS user who submitted the calculated dates.")
    private String submissionUser;

    @ApiModelProperty(required = true, value = "Key dates to be updated for the offender.")
    private OffenderKeyDates keyDates;
}
