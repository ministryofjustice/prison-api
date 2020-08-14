package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@ApiModel(description = "Offender cell details")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderCell {
    @ApiModelProperty(value = "The case identifier", example = "1")
    private Long id;

    @ApiModelProperty(value = "Description", example = "LEI-1-1")
    private String description;

    @ApiModelProperty(value = "Description", example = "LEI-1-1")
    private String userDescription;

    @ApiModelProperty(value = "Capacity", example = "2")
    private Integer capacity;

    @ApiModelProperty(value = "Number of occupants", example = "2")
    private Integer noOfOccupants;

    @ApiModelProperty(value = "List of attributes")
    private List<OffenderCellAttribute> attributes;

}
