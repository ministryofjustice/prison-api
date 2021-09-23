package uk.gov.justice.hmpps.prison.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "An Offender's address usage")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressUsageDto {
    @ApiModelProperty(value = "Address ID of the associated address", example = "23422313", position = 1)
    private Long addressId;
    @ApiModelProperty(value = "The address usages", example = "HDC", position = 2)
    private String addressUsage;
    @ApiModelProperty(value = "The address usages description", example = "HDC Address", position = 3)
    private String addressUsageDescription;
    @ApiModelProperty(value = "Active Flag", example = "true", position = 4)
    private Boolean activeFlag;
}
