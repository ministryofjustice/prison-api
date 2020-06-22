package net.syscon.elite.api.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "An Offender's address usage")
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AddressUsageDto {
    private Long addressId;
    private String addressUsage;
    private String addressUsageDescription;
    private Boolean activeFlag;
}
