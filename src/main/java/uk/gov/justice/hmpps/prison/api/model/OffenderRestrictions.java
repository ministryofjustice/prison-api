package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@ApiModel(description = "Offender restrictions")
@Data
@AllArgsConstructor
public class OffenderRestrictions {
    @ApiModelProperty(value = "Booking id for offender")
    private Long bookingId;

    @ApiModelProperty(value = "Offender restrictions")
    final List<OffenderRestriction> offenderRestrictions;
}
