package uk.gov.justice.hmpps.prison.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@ApiModel(description = "Military Records")
@Data
@AllArgsConstructor
public class MilitaryRecords {
    @ApiModelProperty(value = "Military Records")
    final List<MilitaryRecord> militaryRecords;
}
