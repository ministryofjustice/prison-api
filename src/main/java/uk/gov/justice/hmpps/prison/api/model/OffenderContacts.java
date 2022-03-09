package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@ApiModel(description = "Offender contacts")
@Data
@AllArgsConstructor
public class OffenderContacts {
    @ApiModelProperty(value = "Offender contacts")
    final List<OffenderContact> offenderContacts;
}
